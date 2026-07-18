package dev.ergenverse.simulation.artifact;

import dev.ergenverse.canon.Provenance;
import dev.ergenverse.cultivation.RealmId;

import java.util.List;

/**
 * AUTO-GENERATED artifact usage profiles for Wang Lin's arsenal.
 *
 * <p>This file contains profiles for items that don't have
 * hand-written profiles in {@link ArtifactUsageProfiles}.
 * Each profile is based on the item's category and estimated
 * artifact realm from canon knowledge.
 *
 * <p>Hand-written profiles (in ArtifactUsageProfiles.java) take
 * precedence when both exist for the same item.
 */
public final class ArtifactUsageProfilesGenerated {

    private static boolean bootstrapped = false;

    private ArtifactUsageProfilesGenerated() {}

    // ── Ancient Ancestors Finger Attack ──
    public static final ArtifactUsageProfile ANCIENT_ANCESTORS_FINGER_ATTACK = ArtifactUsageProfile.builder(
            "wanglin/ancient_ancestors_finger_attack", "Ancient Ancestors Finger Attack", "Ancient Ancestors Finger Attack")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Ancient Bloodline Ancestor ──
    public static final ArtifactUsageProfile ANCIENT_BLOODLINE_ANCESTOR = ArtifactUsageProfile.builder(
            "wanglin/ancient_bloodline_ancestor", "Ancient Bloodline Ancestor", "Ancient Bloodline Ancestor")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Ancient Demon ──
    public static final ArtifactUsageProfile ANCIENT_DEMON = ArtifactUsageProfile.builder(
            "wanglin/ancient_demon", "Ancient Demon", "Ancient Demon")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Ancient Devil ──
    public static final ArtifactUsageProfile ANCIENT_DEVIL = ArtifactUsageProfile.builder(
            "wanglin/ancient_devil", "Ancient Devil", "Ancient Devil")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Ancient God Body ──
    public static final ArtifactUsageProfile ANCIENT_GOD_BODY = ArtifactUsageProfile.builder(
            "wanglin/ancient_god_body", "Ancient God Body", "Ancient God Body")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NIRVANA_FRUIT)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Ancient God Bracer ──
    public static final ArtifactUsageProfile ANCIENT_GOD_BRACER = ArtifactUsageProfile.builder(
            "wanglin/ancient_god_bracer", "Ancient God Bracer", "Ancient God Bracer")
            .category("Defense / Armor")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1231.8)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.SPIRIT_SEIZER)))
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Ancient God Furnace ──
    public static final ArtifactUsageProfile ANCIENT_GOD_FURNACE = ArtifactUsageProfile.builder(
            "wanglin/ancient_god_furnace", "Ancient God Furnace", "Ancient God Furnace")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2053.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Ancient God Leather Armor ──
    public static final ArtifactUsageProfile ANCIENT_GOD_LEATHER_ARMOR = ArtifactUsageProfile.builder(
            "wanglin/ancient_god_leather_armor", "Ancient God Leather Armor", "Ancient God Leather Armor")
            .category("Defense / Armor")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1231.8)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.SPIRIT_SEIZER)))
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Ancient God Tactic ──
    public static final ArtifactUsageProfile ANCIENT_GOD_TACTIC = ArtifactUsageProfile.builder(
            "wanglin/ancient_god_tactic", "Ancient God Tactic", "Ancient God Tactic")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1231.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Ancient God Trident ──
    public static final ArtifactUsageProfile ANCIENT_GOD_TRIDENT = ArtifactUsageProfile.builder(
            "wanglin/ancient_god_trident", "Ancient God Trident", "Ancient God Trident")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1231.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Ancient Leaf ──
    public static final ArtifactUsageProfile ANCIENT_LEAF = ArtifactUsageProfile.builder(
            "wanglin/ancient_leaf", "Ancient Leaf", "Ancient Leaf")
            .category("Miscellaneous")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4.2)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Ancient Restrictions ──
    public static final ArtifactUsageProfile ANCIENT_RESTRICTIONS = ArtifactUsageProfile.builder(
            "wanglin/ancient_restrictions", "Ancient Restrictions", "Ancient Restrictions")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(44.4)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "AncientRestrictions Activation", "Ancient Restrictions",
                                    "Activate the Ancient Restrictions.",
                                    0.5, 0.4, RealmId.ASCENDANT,
                                    "The Ancient Restrictions activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Ancient Soul Restriction ──
    public static final ArtifactUsageProfile ANCIENT_SOUL_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/ancient_soul_restriction", "Ancient Soul Restriction", "Ancient Soul Restriction")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4921.2)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "AncientSoulRestriction Activation", "Ancient Soul Restriction",
                                    "Activate the Ancient Soul Restriction.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Ancient Soul Restriction activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Ancient Soul Restriction Tortoise Beast ──
    public static final ArtifactUsageProfile ANCIENT_SOUL_RESTRICTION_TORTOISE_BEAST = ArtifactUsageProfile.builder(
            "wanglin/ancient_soul_restriction_tortoise_beast", "Ancient Soul Restriction Tortoise Beast", "Ancient Soul Restriction Tortoise Beast")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4921.2)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "AncientSoulRestrictionTortoiseBeast Activation", "Ancient Soul Restriction Tortoise Beast",
                                    "Activate the Ancient Soul Restriction Tortoise Beast.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Ancient Soul Restriction Tortoise Beast activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Ancient Thunder Dragon Form ──
    public static final ArtifactUsageProfile ANCIENT_THUNDER_DRAGON_FORM = ArtifactUsageProfile.builder(
            "wanglin/ancient_thunder_dragon_form", "Ancient Thunder Dragon Form", "Ancient Thunder Dragon Form")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Annihilation Restriction ──
    public static final ArtifactUsageProfile ANNIHILATION_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/annihilation_restriction", "Annihilation Restriction", "Annihilation Restriction")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1234.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "AnnihilationRestriction Activation", "Annihilation Restriction",
                                    "Activate the Annihilation Restriction.",
                                    0.5, 0.4, RealmId.NIRVANA_FRUIT,
                                    "The Annihilation Restriction activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Attractive Force Art ──
    public static final ArtifactUsageProfile ATTRACTIVE_FORCE_ART = ArtifactUsageProfile.builder(
            "wanglin/attractive_force_art", "Attractive Force Art", "Attractive Force Art")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "AttractiveForceArt Activation", "Attractive Force Art",
                                    "Activate the Attractive Force Art.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Attractive Force Art activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Azure Ancient God Shield ──
    public static final ArtifactUsageProfile AZURE_ANCIENT_GOD_SHIELD = ArtifactUsageProfile.builder(
            "wanglin/azure_ancient_god_shield", "Azure Ancient God Shield", "Azure Ancient God Shield")
            .category("Defense / Armor")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1231.8)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.SPIRIT_SEIZER)))
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Basic Formation Book ──
    public static final ArtifactUsageProfile BASIC_FORMATION_BOOK = ArtifactUsageProfile.builder(
            "wanglin/basic_formation_book", "Basic Formation Book", "Basic Formation Book")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(8.4)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "BasicFormationBook Activation", "Basic Formation Book",
                                    "Activate the Basic Formation Book.",
                                    0.5, 0.4, RealmId.CORE_FORMATION,
                                    "The Basic Formation Book activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Battle Will Domain ──
    public static final ArtifactUsageProfile BATTLE_WILL_DOMAIN = ArtifactUsageProfile.builder(
            "wanglin/battle_will_domain", "Battle Will Domain", "Battle Will Domain")
            .category("Miscellaneous")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2460.6)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Beads Seven Colored Realm ──
    public static final ArtifactUsageProfile BEADS_SEVEN_COLORED_REALM = ArtifactUsageProfile.builder(
            "wanglin/beads_seven_colored_realm", "Beads Seven Colored Realm", "Beads Seven Colored Realm")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Beast Skin Tattoo ──
    public static final ArtifactUsageProfile BEAST_SKIN_TATTOO = ArtifactUsageProfile.builder(
            "wanglin/beast_skin_tattoo", "Beast Skin Tattoo", "Beast Skin Tattoo")
            .category("Beast / Companion")
            .artifactRealm(RealmId.QI_CONDENSATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(22.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.QI_CONDENSATION)
            .build();

    // ── Bell Sealing Tracking ──
    public static final ArtifactUsageProfile BELL_SEALING_TRACKING = ArtifactUsageProfile.builder(
            "wanglin/bell_sealing_tracking", "Bell Sealing Tracking", "Bell Sealing Tracking")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "BellSealingTracking Activation", "Bell Sealing Tracking",
                                    "Activate the Bell Sealing Tracking.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Bell Sealing Tracking activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Billion Soul Flag ──
    public static final ArtifactUsageProfile BILLION_SOUL_FLAG = ArtifactUsageProfile.builder(
            "wanglin/billion_soul_flag", "Billion Soul Flag", "Billion Soul Flag")
            .category("Flag / Banner")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2058.0)
            .attackSpeed(0.7)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Black Comb 19 Teeth ──
    public static final ArtifactUsageProfile BLACK_COMB_19_TEETH = ArtifactUsageProfile.builder(
            "wanglin/black_comb_19_teeth", "Black Comb 19 Teeth", "Black Comb 19 Teeth")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.QI_CONDENSATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.5)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.QI_CONDENSATION)
            .build();

    // ── Blood Ancestors Blood Body ──
    public static final ArtifactUsageProfile BLOOD_ANCESTORS_BLOOD_BODY = ArtifactUsageProfile.builder(
            "wanglin/blood_ancestors_blood_body", "Blood Ancestors Blood Body", "Blood Ancestors Blood Body")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NASCENT_SOUL)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Blood Jades Yao Xixue ──
    public static final ArtifactUsageProfile BLOOD_JADES_YAO_XIXUE = ArtifactUsageProfile.builder(
            "wanglin/blood_jades_yao_xixue", "Blood Jades Yao Xixue", "Blood Jades Yao Xixue")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Blood Lines Rules Restriction ──
    public static final ArtifactUsageProfile BLOOD_LINES_RULES_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/blood_lines_rules_restriction", "Blood Lines Rules Restriction", "Blood Lines Rules Restriction")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(44.4)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "BloodLinesRulesRestriction Activation", "Blood Lines Rules Restriction",
                                    "Activate the Blood Lines Rules Restriction.",
                                    0.5, 0.4, RealmId.ASCENDANT,
                                    "The Blood Lines Rules Restriction activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Blood Nascent Soul Seven Colored ──
    public static final ArtifactUsageProfile BLOOD_NASCENT_SOUL_SEVEN_COLORED = ArtifactUsageProfile.builder(
            "wanglin/blood_nascent_soul_seven_colored", "Blood Nascent Soul Seven Colored", "Blood Nascent Soul Seven Colored")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Blood Pavilion ──
    public static final ArtifactUsageProfile BLOOD_PAVILION = ArtifactUsageProfile.builder(
            "wanglin/blood_pavilion", "Blood Pavilion", "Blood Pavilion")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1029.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Blood Red Nascent Soul ──
    public static final ArtifactUsageProfile BLOOD_RED_NASCENT_SOUL = ArtifactUsageProfile.builder(
            "wanglin/blood_red_nascent_soul", "Blood Red Nascent Soul", "Blood Red Nascent Soul")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Blood Refining Technique ──
    public static final ArtifactUsageProfile BLOOD_REFINING_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/blood_refining_technique", "Blood Refining Technique", "Blood Refining Technique")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "BloodRefiningTechnique Activation", "Blood Refining Technique",
                                    "Activate the Blood Refining Technique.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Blood Refining Technique activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Blood Slaughter Sword ──
    public static final ArtifactUsageProfile BLOOD_SLAUGHTER_SWORD = ArtifactUsageProfile.builder(
            "wanglin/blood_slaughter_sword", "Blood Slaughter Sword", "Blood Slaughter Sword")
            .category("Weapon")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(27.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "BloodSlaughterSword Activation", "Blood Slaughter Sword",
                                    "Activate the Blood Slaughter Sword.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Blood Slaughter Sword activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Bloodline Thunder ──
    public static final ArtifactUsageProfile BLOODLINE_THUNDER = ArtifactUsageProfile.builder(
            "wanglin/bloodline_thunder", "Bloodline Thunder", "Bloodline Thunder")
            .category("Elemental")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(21.6)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "BloodlineThunder Activation", "Bloodline Thunder",
                                    "Activate the Bloodline Thunder.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Bloodline Thunder activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Blue Flames ──
    public static final ArtifactUsageProfile BLUE_FLAMES = ArtifactUsageProfile.builder(
            "wanglin/blue_flames", "Blue Flames", "Blue Flames")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Blue Umbrella ──
    public static final ArtifactUsageProfile BLUE_UMBRELLA = ArtifactUsageProfile.builder(
            "wanglin/blue_umbrella", "Blue Umbrella", "Blue Umbrella")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Body Fixation Art ──
    public static final ArtifactUsageProfile BODY_FIXATION_ART = ArtifactUsageProfile.builder(
            "wanglin/body_fixation_art", "Body Fixation Art", "Body Fixation Art")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NIRVANA_FRUIT)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Body Fixation Art Xiangang ──
    public static final ArtifactUsageProfile BODY_FIXATION_ART_XIANGANG = ArtifactUsageProfile.builder(
            "wanglin/body_fixation_art_xiangang", "Body Fixation Art Xiangang", "Body Fixation Art Xiangang")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NIRVANA_FRUIT)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Body Formation ──
    public static final ArtifactUsageProfile BODY_FORMATION = ArtifactUsageProfile.builder(
            "wanglin/body_formation", "Body Formation", "Body Formation")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(44.4)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "BodyFormation Activation", "Body Formation",
                                    "Activate the Body Formation.",
                                    0.5, 0.4, RealmId.ASCENDANT,
                                    "The Body Formation activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Brilliant Void Sea Dragon ──
    public static final ArtifactUsageProfile BRILLIANT_VOID_SEA_DRAGON = ArtifactUsageProfile.builder(
            "wanglin/brilliant_void_sea_dragon", "Brilliant Void Sea Dragon", "Brilliant Void Sea Dragon")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Bronze Mirror Time Domain ──
    public static final ArtifactUsageProfile BRONZE_MIRROR_TIME_DOMAIN = ArtifactUsageProfile.builder(
            "wanglin/bronze_mirror_time_domain", "Bronze Mirror Time Domain", "Bronze Mirror Time Domain")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Burning Realm Ancient Umbrella ──
    public static final ArtifactUsageProfile BURNING_REALM_ANCIENT_UMBRELLA = ArtifactUsageProfile.builder(
            "wanglin/burning_realm_ancient_umbrella", "Burning Realm Ancient Umbrella", "Burning Realm Ancient Umbrella")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Burning Realm Ancient Umbrella Dao ──
    public static final ArtifactUsageProfile BURNING_REALM_ANCIENT_UMBRELLA_DAO = ArtifactUsageProfile.builder(
            "wanglin/burning_realm_ancient_umbrella_dao", "Burning Realm Ancient Umbrella Dao", "Burning Realm Ancient Umbrella Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "BurningRealmAncientUmbrellaDao Activation", "Burning Realm Ancient Umbrella Dao",
                                    "Activate the Burning Realm Ancient Umbrella Dao.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Burning Realm Ancient Umbrella Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Call Wind Summon Rain Magic Arsenal ──
    public static final ArtifactUsageProfile CALL_WIND_SUMMON_RAIN_MAGIC_ARSENAL = ArtifactUsageProfile.builder(
            "wanglin/call_wind_summon_rain_magic_arsenal", "Call Wind Summon Rain Magic Arsenal", "Call Wind Summon Rain Magic Arsenal")
            .category("Elemental")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(31.2)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "CallWindSummonRainMagicArsenal Activation", "Call Wind Summon Rain Magic Arsenal",
                                    "Activate the Call Wind Summon Rain Magic Arsenal.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Call Wind Summon Rain Magic Arsenal activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Carving Domain Of Time ──
    public static final ArtifactUsageProfile CARVING_DOMAIN_OF_TIME = ArtifactUsageProfile.builder(
            "wanglin/carving_domain_of_time", "Carving Domain Of Time", "Carving Domain Of Time")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Celestial Bloodline Ancestor ──
    public static final ArtifactUsageProfile CELESTIAL_BLOODLINE_ANCESTOR = ArtifactUsageProfile.builder(
            "wanglin/celestial_bloodline_ancestor", "Celestial Bloodline Ancestor", "Celestial Bloodline Ancestor")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1231.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Celestial Body ──
    public static final ArtifactUsageProfile CELESTIAL_BODY = ArtifactUsageProfile.builder(
            "wanglin/celestial_body", "Celestial Body", "Celestial Body")
            .category("Defense / Armor")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2460.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.TRUE_IMMORTAL)))
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Celestial Capture Net ──
    public static final ArtifactUsageProfile CELESTIAL_CAPTURE_NET = ArtifactUsageProfile.builder(
            "wanglin/celestial_capture_net", "Celestial Capture Net", "Celestial Capture Net")
            .category("Miscellaneous")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2460.6)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Celestial Emperor Crown ──
    public static final ArtifactUsageProfile CELESTIAL_EMPEROR_CROWN = ArtifactUsageProfile.builder(
            "wanglin/celestial_emperor_crown", "Celestial Emperor Crown", "Celestial Emperor Crown")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1029.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Celestial Mountain Soul ──
    public static final ArtifactUsageProfile CELESTIAL_MOUNTAIN_SOUL = ArtifactUsageProfile.builder(
            "wanglin/celestial_mountain_soul", "Celestial Mountain Soul", "Celestial Mountain Soul")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1231.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Celestial Sealing Stamp ──
    public static final ArtifactUsageProfile CELESTIAL_SEALING_STAMP = ArtifactUsageProfile.builder(
            "wanglin/celestial_sealing_stamp", "Celestial Sealing Stamp", "Celestial Sealing Stamp")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2463.6)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "CelestialSealingStamp Activation", "Celestial Sealing Stamp",
                                    "Activate the Celestial Sealing Stamp.",
                                    0.5, 0.4, RealmId.SPIRIT_SEIZER,
                                    "The Celestial Sealing Stamp activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Celestial Slaughter Art ──
    public static final ArtifactUsageProfile CELESTIAL_SLAUGHTER_ART = ArtifactUsageProfile.builder(
            "wanglin/celestial_slaughter_art", "Celestial Slaughter Art", "Celestial Slaughter Art")
            .category("Technique")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(3284.8)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "CelestialSlaughterArt Activation", "Celestial Slaughter Art",
                                    "Activate the Celestial Slaughter Art.",
                                    0.5, 0.4, RealmId.SPIRIT_SEIZER,
                                    "The Celestial Slaughter Art activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Celestial Spirit Clock ──
    public static final ArtifactUsageProfile CELESTIAL_SPIRIT_CLOCK = ArtifactUsageProfile.builder(
            "wanglin/celestial_spirit_clock", "Celestial Spirit Clock", "Celestial Spirit Clock")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2053.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Celestial Wine Jug ──
    public static final ArtifactUsageProfile CELESTIAL_WINE_JUG = ArtifactUsageProfile.builder(
            "wanglin/celestial_wine_jug", "Celestial Wine Jug", "Celestial Wine Jug")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4101.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Cloak Vermilion Bird Emperor ──
    public static final ArtifactUsageProfile CLOAK_VERMILION_BIRD_EMPEROR = ArtifactUsageProfile.builder(
            "wanglin/cloak_vermilion_bird_emperor", "Cloak Vermilion Bird Emperor", "Cloak Vermilion Bird Emperor")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Collection Pavilion ──
    public static final ArtifactUsageProfile COLLECTION_PAVILION = ArtifactUsageProfile.builder(
            "wanglin/collection_pavilion", "Collection Pavilion", "Collection Pavilion")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1029.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Copper Celestial Guard Du Jian ──
    public static final ArtifactUsageProfile COPPER_CELESTIAL_GUARD_DU_JIAN = ArtifactUsageProfile.builder(
            "wanglin/copper_celestial_guard_du_jian", "Copper Celestial Guard Du Jian", "Copper Celestial Guard Du Jian")
            .category("Defense / Armor")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2460.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.TRUE_IMMORTAL)))
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Crystal Sword ──
    public static final ArtifactUsageProfile CRYSTAL_SWORD = ArtifactUsageProfile.builder(
            "wanglin/crystal_sword", "Crystal Sword", "Crystal Sword")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(39.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "CrystalSword Activation", "Crystal Sword",
                                    "Activate the Crystal Sword.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Crystal Sword activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Cultivator Dao Avatar ──
    public static final ArtifactUsageProfile CULTIVATOR_DAO_AVATAR = ArtifactUsageProfile.builder(
            "wanglin/cultivator_dao_avatar", "Cultivator Dao Avatar", "Cultivator Dao Avatar")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "CultivatorDaoAvatar Activation", "Cultivator Dao Avatar",
                                    "Activate the Cultivator Dao Avatar.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Cultivator Dao Avatar activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Dagger Ge Hong ──
    public static final ArtifactUsageProfile DAGGER_GE_HONG = ArtifactUsageProfile.builder(
            "wanglin/dagger_ge_hong", "Dagger Ge Hong", "Dagger Ge Hong")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(39.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DaggerGeHong Activation", "Dagger Ge Hong",
                                    "Activate the Dagger Ge Hong.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Dagger Ge Hong activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Dao Fusion ──
    public static final ArtifactUsageProfile DAO_FUSION = ArtifactUsageProfile.builder(
            "wanglin/dao_fusion", "Dao Fusion", "Dao Fusion")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1861.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_CLEANSER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DaoFusion Activation", "Dao Fusion",
                                    "Activate the Dao Fusion.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Dao Fusion activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Dao Karma ──
    public static final ArtifactUsageProfile DAO_KARMA = ArtifactUsageProfile.builder(
            "wanglin/dao_karma", "Dao Karma", "Dao Karma")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1861.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_CLEANSER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DaoKarma Activation", "Dao Karma",
                                    "Activate the Dao Karma.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Dao Karma activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Dao Life Death ──
    public static final ArtifactUsageProfile DAO_LIFE_DEATH = ArtifactUsageProfile.builder(
            "wanglin/dao_life_death", "Dao Life Death", "Dao Life Death")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1861.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_CLEANSER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DaoLifeDeath Activation", "Dao Life Death",
                                    "Activate the Dao Life Death.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Dao Life Death activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Dao Slaughter ──
    public static final ArtifactUsageProfile DAO_SLAUGHTER = ArtifactUsageProfile.builder(
            "wanglin/dao_slaughter", "Dao Slaughter", "Dao Slaughter")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(3704.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_FRUIT)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DaoSlaughter Activation", "Dao Slaughter",
                                    "Activate the Dao Slaughter.",
                                    0.5, 0.4, RealmId.NIRVANA_FRUIT,
                                    "The Dao Slaughter activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Dao Time ──
    public static final ArtifactUsageProfile DAO_TIME = ArtifactUsageProfile.builder(
            "wanglin/dao_time", "Dao Time", "Dao Time")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DaoTime Activation", "Dao Time",
                                    "Activate the Dao Time.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Dao Time activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Dao Transformation Yellow Springs ──
    public static final ArtifactUsageProfile DAO_TRANSFORMATION_YELLOW_SPRINGS = ArtifactUsageProfile.builder(
            "wanglin/dao_transformation_yellow_springs", "Dao Transformation Yellow Springs", "Dao Transformation Yellow Springs")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DaoTransformationYellowSprings Activation", "Dao Transformation Yellow Springs",
                                    "Activate the Dao Transformation Yellow Springs.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Dao Transformation Yellow Springs activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Dark Green Flying Sword ──
    public static final ArtifactUsageProfile DARK_GREEN_FLYING_SWORD = ArtifactUsageProfile.builder(
            "wanglin/dark_green_flying_sword", "Dark Green Flying Sword", "Dark Green Flying Sword")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(39.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DarkGreenFlyingSword Activation", "Dark Green Flying Sword",
                                    "Activate the Dark Green Flying Sword.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Dark Green Flying Sword activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Dark Heaven Stone ──
    public static final ArtifactUsageProfile DARK_HEAVEN_STONE = ArtifactUsageProfile.builder(
            "wanglin/dark_heaven_stone", "Dark Heaven Stone", "Dark Heaven Stone")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.QI_CONDENSATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.5)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.QI_CONDENSATION)
            .build();

    // ── Dark Moon Clear Skies ──
    public static final ArtifactUsageProfile DARK_MOON_CLEAR_SKIES = ArtifactUsageProfile.builder(
            "wanglin/dark_moon_clear_skies", "Dark Moon Clear Skies", "Dark Moon Clear Skies")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Defying Thunder ──
    public static final ArtifactUsageProfile DEFYING_THUNDER = ArtifactUsageProfile.builder(
            "wanglin/defying_thunder", "Defying Thunder", "Defying Thunder")
            .category("Elemental")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(21.6)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DefyingThunder Activation", "Defying Thunder",
                                    "Activate the Defying Thunder.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Defying Thunder activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Demon Blade Earth Burial ──
    public static final ArtifactUsageProfile DEMON_BLADE_EARTH_BURIAL = ArtifactUsageProfile.builder(
            "wanglin/demon_blade_earth_burial", "Demon Blade Earth Burial", "Demon Blade Earth Burial")
            .category("Weapon")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(27.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DemonBladeEarthBurial Activation", "Demon Blade Earth Burial",
                                    "Activate the Demon Blade Earth Burial.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Demon Blade Earth Burial activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Demon Spell Wind Fire Mountain ──
    public static final ArtifactUsageProfile DEMON_SPELL_WIND_FIRE_MOUNTAIN = ArtifactUsageProfile.builder(
            "wanglin/demon_spell_wind_fire_mountain", "Demon Spell Wind Fire Mountain", "Demon Spell Wind Fire Mountain")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Demonic Finger ──
    public static final ArtifactUsageProfile DEMONIC_FINGER = ArtifactUsageProfile.builder(
            "wanglin/demonic_finger", "Demonic Finger", "Demonic Finger")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Destruction Restriction ──
    public static final ArtifactUsageProfile DESTRUCTION_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/destruction_restriction", "Destruction Restriction", "Destruction Restriction")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DestructionRestriction Activation", "Destruction Restriction",
                                    "Activate the Destruction Restriction.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Destruction Restriction activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Devil Dao Life Death Reverse ──
    public static final ArtifactUsageProfile DEVIL_DAO_LIFE_DEATH_REVERSE = ArtifactUsageProfile.builder(
            "wanglin/devil_dao_life_death_reverse", "Devil Dao Life Death Reverse", "Devil Dao Life Death Reverse")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1861.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_CLEANSER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DevilDaoLifeDeathReverse Activation", "Devil Dao Life Death Reverse",
                                    "Activate the Devil Dao Life Death Reverse.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Devil Dao Life Death Reverse activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Devil Sky Cloud Monkey ──
    public static final ArtifactUsageProfile DEVIL_SKY_CLOUD_MONKEY = ArtifactUsageProfile.builder(
            "wanglin/devil_sky_cloud_monkey", "Devil Sky Cloud Monkey", "Devil Sky Cloud Monkey")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Devil Soul Bottle ──
    public static final ArtifactUsageProfile DEVIL_SOUL_BOTTLE = ArtifactUsageProfile.builder(
            "wanglin/devil_soul_bottle", "Devil Soul Bottle", "Devil Soul Bottle")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Devil Soul Tornado Leader ──
    public static final ArtifactUsageProfile DEVIL_SOUL_TORNADO_LEADER = ArtifactUsageProfile.builder(
            "wanglin/devil_soul_tornado_leader", "Devil Soul Tornado Leader", "Devil Soul Tornado Leader")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Devil Xu Liguo First ──
    public static final ArtifactUsageProfile DEVIL_XU_LIGUO_FIRST = ArtifactUsageProfile.builder(
            "wanglin/devil_xu_liguo_first", "Devil Xu Liguo First", "Devil Xu Liguo First")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Devilish Flames Fire Dragon ──
    public static final ArtifactUsageProfile DEVILISH_FLAMES_FIRE_DRAGON = ArtifactUsageProfile.builder(
            "wanglin/devilish_flames_fire_dragon", "Devilish Flames Fire Dragon", "Devilish Flames Fire Dragon")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Devouring Technique ──
    public static final ArtifactUsageProfile DEVOURING_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/devouring_technique", "Devouring Technique", "Devouring Technique")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1029.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Disguising Technique ──
    public static final ArtifactUsageProfile DISGUISING_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/disguising_technique", "Disguising Technique", "Disguising Technique")
            .category("Technique")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(20.8)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DisguisingTechnique Activation", "Disguising Technique",
                                    "Activate the Disguising Technique.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Disguising Technique activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Divine Path ──
    public static final ArtifactUsageProfile DIVINE_PATH = ArtifactUsageProfile.builder(
            "wanglin/divine_path", "Divine Path", "Divine Path")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Divine Path Clone ──
    public static final ArtifactUsageProfile DIVINE_PATH_CLONE = ArtifactUsageProfile.builder(
            "wanglin/divine_path_clone", "Divine Path Clone", "Divine Path Clone")
            .category("Clone / Avatar")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(18.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Dream Dao ──
    public static final ArtifactUsageProfile DREAM_DAO = ArtifactUsageProfile.builder(
            "wanglin/dream_dao", "Dream Dao", "Dream Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DreamDao Activation", "Dream Dao",
                                    "Activate the Dream Dao.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Dream Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Dream Dao Mirror ──
    public static final ArtifactUsageProfile DREAM_DAO_MIRROR = ArtifactUsageProfile.builder(
            "wanglin/dream_dao_mirror", "Dream Dao Mirror", "Dream Dao Mirror")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "DreamDaoMirror Activation", "Dream Dao Mirror",
                                    "Activate the Dream Dao Mirror.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Dream Dao Mirror activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Earth Escape Technique ──
    public static final ArtifactUsageProfile EARTH_ESCAPE_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/earth_escape_technique", "Earth Escape Technique", "Earth Escape Technique")
            .category("Technique")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(20.8)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "EarthEscapeTechnique Activation", "Earth Escape Technique",
                                    "Activate the Earth Escape Technique.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Earth Escape Technique activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Earth Essence ──
    public static final ArtifactUsageProfile EARTH_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/earth_essence", "Earth Essence", "Earth Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "EarthEssence Activation", "Earth Essence",
                                    "Activate the Earth Essence.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Earth Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Earth Palace ──
    public static final ArtifactUsageProfile EARTH_PALACE = ArtifactUsageProfile.builder(
            "wanglin/earth_palace", "Earth Palace", "Earth Palace")
            .category("Technique")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1646.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "EarthPalace Activation", "Earth Palace",
                                    "Activate the Earth Palace.",
                                    0.5, 0.4, RealmId.NIRVANA_FRUIT,
                                    "The Earth Palace activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Emerald Bracelet Li Qianmei ──
    public static final ArtifactUsageProfile EMERALD_BRACELET_LI_QIANMEI = ArtifactUsageProfile.builder(
            "wanglin/emerald_bracelet_li_qianmei", "Emerald Bracelet Li Qianmei", "Emerald Bracelet Li Qianmei")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Emperor Furnace ──
    public static final ArtifactUsageProfile EMPEROR_FURNACE = ArtifactUsageProfile.builder(
            "wanglin/emperor_furnace", "Emperor Furnace", "Emperor Furnace")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1029.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Eternal Wood Spirit ──
    public static final ArtifactUsageProfile ETERNAL_WOOD_SPIRIT = ArtifactUsageProfile.builder(
            "wanglin/eternal_wood_spirit", "Eternal Wood Spirit", "Eternal Wood Spirit")
            .category("Elemental")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(21.6)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "EternalWoodSpirit Activation", "Eternal Wood Spirit",
                                    "Activate the Eternal Wood Spirit.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Eternal Wood Spirit activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Ethereal Fire ──
    public static final ArtifactUsageProfile ETHEREAL_FIRE = ArtifactUsageProfile.builder(
            "wanglin/ethereal_fire", "Ethereal Fire", "Ethereal Fire")
            .category("Elemental")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(21.6)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "EtherealFire Activation", "Ethereal Fire",
                                    "Activate the Ethereal Fire.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Ethereal Fire activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Extreme Earth Dao ──
    public static final ArtifactUsageProfile EXTREME_EARTH_DAO = ArtifactUsageProfile.builder(
            "wanglin/extreme_earth_dao", "Extreme Earth Dao", "Extreme Earth Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ExtremeEarthDao Activation", "Extreme Earth Dao",
                                    "Activate the Extreme Earth Dao.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Extreme Earth Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Extreme Fire Dao ──
    public static final ArtifactUsageProfile EXTREME_FIRE_DAO = ArtifactUsageProfile.builder(
            "wanglin/extreme_fire_dao", "Extreme Fire Dao", "Extreme Fire Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ExtremeFireDao Activation", "Extreme Fire Dao",
                                    "Activate the Extreme Fire Dao.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Extreme Fire Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Extreme Fire Dao Imitation ──
    public static final ArtifactUsageProfile EXTREME_FIRE_DAO_IMITATION = ArtifactUsageProfile.builder(
            "wanglin/extreme_fire_dao_imitation", "Extreme Fire Dao Imitation", "Extreme Fire Dao Imitation")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ExtremeFireDaoImitation Activation", "Extreme Fire Dao Imitation",
                                    "Activate the Extreme Fire Dao Imitation.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Extreme Fire Dao Imitation activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Extreme Land Sky Life Dao ──
    public static final ArtifactUsageProfile EXTREME_LAND_SKY_LIFE_DAO = ArtifactUsageProfile.builder(
            "wanglin/extreme_land_sky_life_dao", "Extreme Land Sky Life Dao", "Extreme Land Sky Life Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ExtremeLandSkyLifeDao Activation", "Extreme Land Sky Life Dao",
                                    "Activate the Extreme Land Sky Life Dao.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Extreme Land Sky Life Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Extreme Life Death Dao ──
    public static final ArtifactUsageProfile EXTREME_LIFE_DEATH_DAO = ArtifactUsageProfile.builder(
            "wanglin/extreme_life_death_dao", "Extreme Life Death Dao", "Extreme Life Death Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1861.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_CLEANSER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ExtremeLifeDeathDao Activation", "Extreme Life Death Dao",
                                    "Activate the Extreme Life Death Dao.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Extreme Life Death Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Extreme Metal Dao ──
    public static final ArtifactUsageProfile EXTREME_METAL_DAO = ArtifactUsageProfile.builder(
            "wanglin/extreme_metal_dao", "Extreme Metal Dao", "Extreme Metal Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ExtremeMetalDao Activation", "Extreme Metal Dao",
                                    "Activate the Extreme Metal Dao.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Extreme Metal Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Extreme Sky Dao ──
    public static final ArtifactUsageProfile EXTREME_SKY_DAO = ArtifactUsageProfile.builder(
            "wanglin/extreme_sky_dao", "Extreme Sky Dao", "Extreme Sky Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ExtremeSkyDao Activation", "Extreme Sky Dao",
                                    "Activate the Extreme Sky Dao.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Extreme Sky Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Extreme Water Dao ──
    public static final ArtifactUsageProfile EXTREME_WATER_DAO = ArtifactUsageProfile.builder(
            "wanglin/extreme_water_dao", "Extreme Water Dao", "Extreme Water Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ExtremeWaterDao Activation", "Extreme Water Dao",
                                    "Activate the Extreme Water Dao.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Extreme Water Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Extreme Wood Dao ──
    public static final ArtifactUsageProfile EXTREME_WOOD_DAO = ArtifactUsageProfile.builder(
            "wanglin/extreme_wood_dao", "Extreme Wood Dao", "Extreme Wood Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ExtremeWoodDao Activation", "Extreme Wood Dao",
                                    "Activate the Extreme Wood Dao.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Extreme Wood Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Eyes Suppressing World ──
    public static final ArtifactUsageProfile EYES_SUPPRESSING_WORLD = ArtifactUsageProfile.builder(
            "wanglin/eyes_suppressing_world", "Eyes Suppressing World", "Eyes Suppressing World")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Falling Star ──
    public static final ArtifactUsageProfile FALLING_STAR = ArtifactUsageProfile.builder(
            "wanglin/falling_star", "Falling Star", "Falling Star")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Fate Sealing Ring ──
    public static final ArtifactUsageProfile FATE_SEALING_RING = ArtifactUsageProfile.builder(
            "wanglin/fate_sealing_ring", "Fate Sealing Ring", "Fate Sealing Ring")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.QI_CONDENSATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(6.6)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "FateSealingRing Activation", "Fate Sealing Ring",
                                    "Activate the Fate Sealing Ring.",
                                    0.5, 0.4, RealmId.QI_CONDENSATION,
                                    "The Fate Sealing Ring activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.QI_CONDENSATION)
            .build();

    // ── Fiend Transformation Art ──
    public static final ArtifactUsageProfile FIEND_TRANSFORMATION_ART = ArtifactUsageProfile.builder(
            "wanglin/fiend_transformation_art", "Fiend Transformation Art", "Fiend Transformation Art")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1234.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "FiendTransformationArt Activation", "Fiend Transformation Art",
                                    "Activate the Fiend Transformation Art.",
                                    0.5, 0.4, RealmId.NIRVANA_FRUIT,
                                    "The Fiend Transformation Art activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Finger Of Death ──
    public static final ArtifactUsageProfile FINGER_OF_DEATH = ArtifactUsageProfile.builder(
            "wanglin/finger_of_death", "Finger Of Death", "Finger Of Death")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Fire Bone ──
    public static final ArtifactUsageProfile FIRE_BONE = ArtifactUsageProfile.builder(
            "wanglin/fire_bone", "Fire Bone", "Fire Bone")
            .category("Elemental")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(16.8)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "FireBone Activation", "Fire Bone",
                                    "Activate the Fire Bone.",
                                    0.5, 0.4, RealmId.CORE_FORMATION,
                                    "The Fire Bone activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Fire Essence ──
    public static final ArtifactUsageProfile FIRE_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/fire_essence", "Fire Essence", "Fire Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "FireEssence Activation", "Fire Essence",
                                    "Activate the Fire Essence.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Fire Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Five Elements True Body ──
    public static final ArtifactUsageProfile FIVE_ELEMENTS_TRUE_BODY = ArtifactUsageProfile.builder(
            "wanglin/five_elements_true_body", "Five Elements True Body", "Five Elements True Body")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NASCENT_SOUL)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Five Sword Sheaths ──
    public static final ArtifactUsageProfile FIVE_SWORD_SHEATHS = ArtifactUsageProfile.builder(
            "wanglin/five_sword_sheaths", "Five Sword Sheaths", "Five Sword Sheaths")
            .category("Weapon")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(27.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "FiveSwordSheaths Activation", "Five Sword Sheaths",
                                    "Activate the Five Sword Sheaths.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Five Sword Sheaths activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Flame Dragon ──
    public static final ArtifactUsageProfile FLAME_DRAGON = ArtifactUsageProfile.builder(
            "wanglin/flame_dragon", "Flame Dragon", "Flame Dragon")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Flowing Time ──
    public static final ArtifactUsageProfile FLOWING_TIME = ArtifactUsageProfile.builder(
            "wanglin/flowing_time", "Flowing Time", "Flowing Time")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Fog Devil Lance ──
    public static final ArtifactUsageProfile FOG_DEVIL_LANCE = ArtifactUsageProfile.builder(
            "wanglin/fog_devil_lance", "Fog Devil Lance", "Fog Devil Lance")
            .category("Weapon")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(27.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "FogDevilLance Activation", "Fog Devil Lance",
                                    "Activate the Fog Devil Lance.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Fog Devil Lance activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Fog Transformation Thunder Spell ──
    public static final ArtifactUsageProfile FOG_TRANSFORMATION_THUNDER_SPELL = ArtifactUsageProfile.builder(
            "wanglin/fog_transformation_thunder_spell", "Fog Transformation Thunder Spell", "Fog Transformation Thunder Spell")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(15.6)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "FogTransformationThunderSpell Activation", "Fog Transformation Thunder Spell",
                                    "Activate the Fog Transformation Thunder Spell.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Fog Transformation Thunder Spell activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Foundation Stealing Technique ──
    public static final ArtifactUsageProfile FOUNDATION_STEALING_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/foundation_stealing_technique", "Foundation Stealing Technique", "Foundation Stealing Technique")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "FoundationStealingTechnique Activation", "Foundation Stealing Technique",
                                    "Activate the Foundation Stealing Technique.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Foundation Stealing Technique activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Fragment Stamp Celestial Sealing ──
    public static final ArtifactUsageProfile FRAGMENT_STAMP_CELESTIAL_SEALING = ArtifactUsageProfile.builder(
            "wanglin/fragment_stamp_celestial_sealing", "Fragment Stamp Celestial Sealing", "Fragment Stamp Celestial Sealing")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2463.6)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "FragmentStampCelestialSealing Activation", "Fragment Stamp Celestial Sealing",
                                    "Activate the Fragment Stamp Celestial Sealing.",
                                    0.5, 0.4, RealmId.SPIRIT_SEIZER,
                                    "The Fragment Stamp Celestial Sealing activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Ghostly Sail Main ──
    public static final ArtifactUsageProfile GHOSTLY_SAIL_MAIN = ArtifactUsageProfile.builder(
            "wanglin/ghostly_sail_main", "Ghostly Sail Main", "Ghostly Sail Main")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Giant Head Skull Ancient Clansman ──
    public static final ArtifactUsageProfile GIANT_HEAD_SKULL_ANCIENT_CLANSMAN = ArtifactUsageProfile.builder(
            "wanglin/giant_head_skull_ancient_clansman", "Giant Head Skull Ancient Clansman", "Giant Head Skull Ancient Clansman")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Giant Thunder Stamp ──
    public static final ArtifactUsageProfile GIANT_THUNDER_STAMP = ArtifactUsageProfile.builder(
            "wanglin/giant_thunder_stamp", "Giant Thunder Stamp", "Giant Thunder Stamp")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── God Demon Devil Ancient Dao No Celestial ──
    public static final ArtifactUsageProfile GOD_DEMON_DEVIL_ANCIENT_DAO_NO_CELESTIAL = ArtifactUsageProfile.builder(
            "wanglin/god_demon_devil_ancient_dao_no_celestial", "God Demon Devil Ancient Dao No Celestial", "God Demon Devil Ancient Dao No Celestial")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "GodDemonDevilAncientDaoNoCelestial Activation", "God Demon Devil Ancient Dao No Celestial",
                                    "Activate the God Demon Devil Ancient Dao No Celestial.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The God Demon Devil Ancient Dao No Celestial activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── God Punch ──
    public static final ArtifactUsageProfile GOD_PUNCH = ArtifactUsageProfile.builder(
            "wanglin/god_punch", "God Punch", "God Punch")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── God Slaying Seal Origin ──
    public static final ArtifactUsageProfile GOD_SLAYING_SEAL_ORIGIN = ArtifactUsageProfile.builder(
            "wanglin/god_slaying_seal_origin", "God Slaying Seal Origin", "God Slaying Seal Origin")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2463.6)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "GodSlayingSealOrigin Activation", "God Slaying Seal Origin",
                                    "Activate the God Slaying Seal Origin.",
                                    0.5, 0.4, RealmId.SPIRIT_SEIZER,
                                    "The God Slaying Seal Origin activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── God Slaying Spear ──
    public static final ArtifactUsageProfile GOD_SLAYING_SPEAR = ArtifactUsageProfile.builder(
            "wanglin/god_slaying_spear", "God Slaying Spear", "God Slaying Spear")
            .category("Weapon")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(6159.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, true, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "GodSlayingSpear Activation", "God Slaying Spear",
                                    "Activate the God Slaying Spear.",
                                    0.5, 0.4, RealmId.SPIRIT_SEIZER,
                                    "The God Slaying Spear activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── God Slaying War Chariot ──
    public static final ArtifactUsageProfile GOD_SLAYING_WAR_CHARIOT = ArtifactUsageProfile.builder(
            "wanglin/god_slaying_war_chariot", "God Slaying War Chariot", "God Slaying War Chariot")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1231.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── God Tremble Army Formation ──
    public static final ArtifactUsageProfile GOD_TREMBLE_ARMY_FORMATION = ArtifactUsageProfile.builder(
            "wanglin/god_tremble_army_formation", "God Tremble Army Formation", "God Tremble Army Formation")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1234.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "GodTrembleArmyFormation Activation", "God Tremble Army Formation",
                                    "Activate the God Tremble Army Formation.",
                                    0.5, 0.4, RealmId.NIRVANA_FRUIT,
                                    "The God Tremble Army Formation activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Golden Print ──
    public static final ArtifactUsageProfile GOLDEN_PRINT = ArtifactUsageProfile.builder(
            "wanglin/golden_print", "Golden Print", "Golden Print")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Golden Print Xuan Luo ──
    public static final ArtifactUsageProfile GOLDEN_PRINT_XUAN_LUO = ArtifactUsageProfile.builder(
            "wanglin/golden_print_xuan_luo", "Golden Print Xuan Luo", "Golden Print Xuan Luo")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Great Heavenly Venerable Sun ──
    public static final ArtifactUsageProfile GREAT_HEAVENLY_VENERABLE_SUN = ArtifactUsageProfile.builder(
            "wanglin/great_heavenly_venerable_sun", "Great Heavenly Venerable Sun", "Great Heavenly Venerable Sun")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Great Teleportation Forbidden ──
    public static final ArtifactUsageProfile GREAT_TELEPORTATION_FORBIDDEN = ArtifactUsageProfile.builder(
            "wanglin/great_teleportation_forbidden", "Great Teleportation Forbidden", "Great Teleportation Forbidden")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Green Fragment Power Heaven ──
    public static final ArtifactUsageProfile GREEN_FRAGMENT_POWER_HEAVEN = ArtifactUsageProfile.builder(
            "wanglin/green_fragment_power_heaven", "Green Fragment Power Heaven", "Green Fragment Power Heaven")
            .category("Miscellaneous")
            .artifactRealm(RealmId.QI_CONDENSATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(3.3)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.QI_CONDENSATION)
            .build();

    // ── Gui Yi Sect Earth Armor ──
    public static final ArtifactUsageProfile GUI_YI_SECT_EARTH_ARMOR = ArtifactUsageProfile.builder(
            "wanglin/gui_yi_sect_earth_armor", "Gui Yi Sect Earth Armor", "Gui Yi Sect Earth Armor")
            .category("Defense / Armor")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.CORE_FORMATION)))
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Hairpin Thousand Illusion Ruthless ──
    public static final ArtifactUsageProfile HAIRPIN_THOUSAND_ILLUSION_RUTHLESS = ArtifactUsageProfile.builder(
            "wanglin/hairpin_thousand_illusion_ruthless", "Hairpin Thousand Illusion Ruthless", "Hairpin Thousand Illusion Ruthless")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Half Moon Blade ──
    public static final ArtifactUsageProfile HALF_MOON_BLADE = ArtifactUsageProfile.builder(
            "wanglin/half_moon_blade", "Half Moon Blade", "Half Moon Blade")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(39.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "HalfMoonBlade Activation", "Half Moon Blade",
                                    "Activate the Half Moon Blade.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Half Moon Blade activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Heart Compass Annihilation ──
    public static final ArtifactUsageProfile HEART_COMPASS_ANNIHILATION = ArtifactUsageProfile.builder(
            "wanglin/heart_compass_annihilation", "Heart Compass Annihilation", "Heart Compass Annihilation")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1029.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Heart Of Slaughter ──
    public static final ArtifactUsageProfile HEART_OF_SLAUGHTER = ArtifactUsageProfile.builder(
            "wanglin/heart_of_slaughter", "Heart Of Slaughter", "Heart Of Slaughter")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2053.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Heart Pounding Thunder ──
    public static final ArtifactUsageProfile HEART_POUNDING_THUNDER = ArtifactUsageProfile.builder(
            "wanglin/heart_pounding_thunder", "Heart Pounding Thunder", "Heart Pounding Thunder")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.QI_CONDENSATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.5)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.QI_CONDENSATION)
            .build();

    // ── Heart Restriction ──
    public static final ArtifactUsageProfile HEART_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/heart_restriction", "Heart Restriction", "Heart Restriction")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "HeartRestriction Activation", "Heart Restriction",
                                    "Activate the Heart Restriction.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Heart Restriction activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heaven Avoiding Coffin ──
    public static final ArtifactUsageProfile HEAVEN_AVOIDING_COFFIN = ArtifactUsageProfile.builder(
            "wanglin/heaven_avoiding_coffin", "Heaven Avoiding Coffin", "Heaven Avoiding Coffin")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1029.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Heaven Dao Crystal ──
    public static final ArtifactUsageProfile HEAVEN_DAO_CRYSTAL = ArtifactUsageProfile.builder(
            "wanglin/heaven_dao_crystal", "Heaven Dao Crystal", "Heaven Dao Crystal")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "HeavenDaoCrystal Activation", "Heaven Dao Crystal",
                                    "Activate the Heaven Dao Crystal.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Heaven Dao Crystal activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heaven Defying Bead ──
    public static final ArtifactUsageProfile HEAVEN_DEFYING_BEAD = ArtifactUsageProfile.builder(
            "wanglin/heaven_defying_bead", "Heaven Defying Bead", "Heaven Defying Bead")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(310.2)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Heaven Extinction ──
    public static final ArtifactUsageProfile HEAVEN_EXTINCTION = ArtifactUsageProfile.builder(
            "wanglin/heaven_extinction", "Heaven Extinction", "Heaven Extinction")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Heaven Reversal Stamp ──
    public static final ArtifactUsageProfile HEAVEN_REVERSAL_STAMP = ArtifactUsageProfile.builder(
            "wanglin/heaven_reversal_stamp", "Heaven Reversal Stamp", "Heaven Reversal Stamp")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1029.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Heaven Ripping ──
    public static final ArtifactUsageProfile HEAVEN_RIPPING = ArtifactUsageProfile.builder(
            "wanglin/heaven_ripping", "Heaven Ripping", "Heaven Ripping")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(310.2)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Heaven Splitting Axe Ancestral ──
    public static final ArtifactUsageProfile HEAVEN_SPLITTING_AXE_ANCESTRAL = ArtifactUsageProfile.builder(
            "wanglin/heaven_splitting_axe_ancestral", "Heaven Splitting Axe Ancestral", "Heaven Splitting Axe Ancestral")
            .category("Weapon")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(27.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "HeavenSplittingAxeAncestral Activation", "Heaven Splitting Axe Ancestral",
                                    "Activate the Heaven Splitting Axe Ancestral.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Heaven Splitting Axe Ancestral activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heaven Technique ──
    public static final ArtifactUsageProfile HEAVEN_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/heaven_technique", "Heaven Technique", "Heaven Technique")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "HeavenTechnique Activation", "Heaven Technique",
                                    "Activate the Heaven Technique.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Heaven Technique activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heaven Tiger Flag ──
    public static final ArtifactUsageProfile HEAVEN_TIGER_FLAG = ArtifactUsageProfile.builder(
            "wanglin/heaven_tiger_flag", "Heaven Tiger Flag", "Heaven Tiger Flag")
            .category("Flag / Banner")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(18.0)
            .attackSpeed(0.7)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heavenly Bull Bead ──
    public static final ArtifactUsageProfile HEAVENLY_BULL_BEAD = ArtifactUsageProfile.builder(
            "wanglin/heavenly_bull_bead", "Heavenly Bull Bead", "Heavenly Bull Bead")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heavenly Bull Soul Armour ──
    public static final ArtifactUsageProfile HEAVENLY_BULL_SOUL_ARMOUR = ArtifactUsageProfile.builder(
            "wanglin/heavenly_bull_soul_armour", "Heavenly Bull Soul Armour", "Heavenly Bull Soul Armour")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NASCENT_SOUL)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heavenly Chop ──
    public static final ArtifactUsageProfile HEAVENLY_CHOP = ArtifactUsageProfile.builder(
            "wanglin/heavenly_chop", "Heavenly Chop", "Heavenly Chop")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heavenly Devil Sound ──
    public static final ArtifactUsageProfile HEAVENLY_DEVIL_SOUND = ArtifactUsageProfile.builder(
            "wanglin/heavenly_devil_sound", "Heavenly Devil Sound", "Heavenly Devil Sound")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heavenly Fate Finger ──
    public static final ArtifactUsageProfile HEAVENLY_FATE_FINGER = ArtifactUsageProfile.builder(
            "wanglin/heavenly_fate_finger", "Heavenly Fate Finger", "Heavenly Fate Finger")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Heavenly Flame ──
    public static final ArtifactUsageProfile HEAVENLY_FLAME = ArtifactUsageProfile.builder(
            "wanglin/heavenly_flame", "Heavenly Flame", "Heavenly Flame")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Holy Treasure White Stone ──
    public static final ArtifactUsageProfile HOLY_TREASURE_WHITE_STONE = ArtifactUsageProfile.builder(
            "wanglin/holy_treasure_white_stone", "Holy Treasure White Stone", "Holy Treasure White Stone")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.QI_CONDENSATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.5)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.QI_CONDENSATION)
            .build();

    // ── Human Shaped Armor Green Bull ──
    public static final ArtifactUsageProfile HUMAN_SHAPED_ARMOR_GREEN_BULL = ArtifactUsageProfile.builder(
            "wanglin/human_shaped_armor_green_bull", "Human Shaped Armor Green Bull", "Human Shaped Armor Green Bull")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NASCENT_SOUL)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Ice Imitation Dongling Pool ──
    public static final ArtifactUsageProfile ICE_IMITATION_DONGLING_POOL = ArtifactUsageProfile.builder(
            "wanglin/ice_imitation_dongling_pool", "Ice Imitation Dongling Pool", "Ice Imitation Dongling Pool")
            .category("Elemental")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(21.6)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "IceImitationDonglingPool Activation", "Ice Imitation Dongling Pool",
                                    "Activate the Ice Imitation Dongling Pool.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Ice Imitation Dongling Pool activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Illusionary Circle ──
    public static final ArtifactUsageProfile ILLUSIONARY_CIRCLE = ArtifactUsageProfile.builder(
            "wanglin/illusionary_circle", "Illusionary Circle", "Illusionary Circle")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Immortal Celestial Body ──
    public static final ArtifactUsageProfile IMMORTAL_CELESTIAL_BODY = ArtifactUsageProfile.builder(
            "wanglin/immortal_celestial_body", "Immortal Celestial Body", "Immortal Celestial Body")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NIRVANA_FRUIT)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Immortal Dream ──
    public static final ArtifactUsageProfile IMMORTAL_DREAM = ArtifactUsageProfile.builder(
            "wanglin/immortal_dream", "Immortal Dream", "Immortal Dream")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Infant Skull Da Yi ──
    public static final ArtifactUsageProfile INFANT_SKULL_DA_YI = ArtifactUsageProfile.builder(
            "wanglin/infant_skull_da_yi", "Infant Skull Da Yi", "Infant Skull Da Yi")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Isolation Restriction Compass ──
    public static final ArtifactUsageProfile ISOLATION_RESTRICTION_COMPASS = ArtifactUsageProfile.builder(
            "wanglin/isolation_restriction_compass", "Isolation Restriction Compass", "Isolation Restriction Compass")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(44.4)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "IsolationRestrictionCompass Activation", "Isolation Restriction Compass",
                                    "Activate the Isolation Restriction Compass.",
                                    0.5, 0.4, RealmId.ASCENDANT,
                                    "The Isolation Restriction Compass activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Jade Bottle Black Liquid ──
    public static final ArtifactUsageProfile JADE_BOTTLE_BLACK_LIQUID = ArtifactUsageProfile.builder(
            "wanglin/jade_bottle_black_liquid", "Jade Bottle Black Liquid", "Jade Bottle Black Liquid")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Jade Thunder Defense ──
    public static final ArtifactUsageProfile JADE_THUNDER_DEFENSE = ArtifactUsageProfile.builder(
            "wanglin/jade_thunder_defense", "Jade Thunder Defense", "Jade Thunder Defense")
            .category("Defense / Armor")
            .artifactRealm(RealmId.QI_CONDENSATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(3.3)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.QI_CONDENSATION)))
            .authorityRealm(RealmId.QI_CONDENSATION)
            .build();

    // ── Ji Qiong Head ──
    public static final ArtifactUsageProfile JI_QIONG_HEAD = ArtifactUsageProfile.builder(
            "wanglin/ji_qiong_head", "Ji Qiong Head", "Ji Qiong Head")
            .category("Miscellaneous")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4.2)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Ji Realm ──
    public static final ArtifactUsageProfile JI_REALM = ArtifactUsageProfile.builder(
            "wanglin/ji_realm", "Ji Realm", "Ji Realm")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Ji Realm Execution ──
    public static final ArtifactUsageProfile JI_REALM_EXECUTION = ArtifactUsageProfile.builder(
            "wanglin/ji_realm_execution", "Ji Realm Execution", "Ji Realm Execution")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Karma Domain ──
    public static final ArtifactUsageProfile KARMA_DOMAIN = ArtifactUsageProfile.builder(
            "wanglin/karma_domain", "Karma Domain", "Karma Domain")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(310.2)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Karma Essence ──
    public static final ArtifactUsageProfile KARMA_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/karma_essence", "Karma Essence", "Karma Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1861.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_CLEANSER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "KarmaEssence Activation", "Karma Essence",
                                    "Activate the Karma Essence.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Karma Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Karma Print ──
    public static final ArtifactUsageProfile KARMA_PRINT = ArtifactUsageProfile.builder(
            "wanglin/karma_print", "Karma Print", "Karma Print")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(517.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Karma Whip ──
    public static final ArtifactUsageProfile KARMA_WHIP = ArtifactUsageProfile.builder(
            "wanglin/karma_whip", "Karma Whip", "Karma Whip")
            .category("Weapon")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1551.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "KarmaWhip Activation", "Karma Whip",
                                    "Activate the Karma Whip.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Karma Whip activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Lands Collapse ──
    public static final ArtifactUsageProfile LANDS_COLLAPSE = ArtifactUsageProfile.builder(
            "wanglin/lands_collapse", "Lands Collapse", "Lands Collapse")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Lantern Origin Soul Protection ──
    public static final ArtifactUsageProfile LANTERN_ORIGIN_SOUL_PROTECTION = ArtifactUsageProfile.builder(
            "wanglin/lantern_origin_soul_protection", "Lantern Origin Soul Protection", "Lantern Origin Soul Protection")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Lei Ji Thunder Beast ──
    public static final ArtifactUsageProfile LEI_JI_THUNDER_BEAST = ArtifactUsageProfile.builder(
            "wanglin/lei_ji_thunder_beast", "Lei Ji Thunder Beast", "Lei Ji Thunder Beast")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Li Guang Arrow ──
    public static final ArtifactUsageProfile LI_GUANG_ARROW = ArtifactUsageProfile.builder(
            "wanglin/li_guang_arrow", "Li Guang Arrow", "Li Guang Arrow")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Li Guang Bow ──
    public static final ArtifactUsageProfile LI_GUANG_BOW = ArtifactUsageProfile.builder(
            "wanglin/li_guang_bow", "Li Guang Bow", "Li Guang Bow")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Li Guang Heaven Shattering Bow ──
    public static final ArtifactUsageProfile LI_GUANG_HEAVEN_SHATTERING_BOW = ArtifactUsageProfile.builder(
            "wanglin/li_guang_heaven_shattering_bow", "Li Guang Heaven Shattering Bow", "Li Guang Heaven Shattering Bow")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(13.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Li Guang Heaven Shattering Bow Dao ──
    public static final ArtifactUsageProfile LI_GUANG_HEAVEN_SHATTERING_BOW_DAO = ArtifactUsageProfile.builder(
            "wanglin/li_guang_heaven_shattering_bow_dao", "Li Guang Heaven Shattering Bow Dao", "Li Guang Heaven Shattering Bow Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(46.8)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.SOUL_FORMATION)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "LiGuangHeavenShatteringBowDao Activation", "Li Guang Heaven Shattering Bow Dao",
                                    "Activate the Li Guang Heaven Shattering Bow Dao.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Li Guang Heaven Shattering Bow Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Life Death Domain ──
    public static final ArtifactUsageProfile LIFE_DEATH_DOMAIN = ArtifactUsageProfile.builder(
            "wanglin/life_death_domain", "Life Death Domain", "Life Death Domain")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(310.2)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Life Death Essence ──
    public static final ArtifactUsageProfile LIFE_DEATH_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/life_death_essence", "Life Death Essence", "Life Death Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1861.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_CLEANSER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "LifeDeathEssence Activation", "Life Death Essence",
                                    "Activate the Life Death Essence.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Life Death Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Life Death Restriction ──
    public static final ArtifactUsageProfile LIFE_DEATH_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/life_death_restriction", "Life Death Restriction", "Life Death Restriction")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(620.4)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "LifeDeathRestriction Activation", "Life Death Restriction",
                                    "Activate the Life Death Restriction.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Life Death Restriction activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Life Death Seal ──
    public static final ArtifactUsageProfile LIFE_DEATH_SEAL = ArtifactUsageProfile.builder(
            "wanglin/life_death_seal", "Life Death Seal", "Life Death Seal")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(620.4)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "LifeDeathSeal Activation", "Life Death Seal",
                                    "Activate the Life Death Seal.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Life Death Seal activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Life Seizing Hex ──
    public static final ArtifactUsageProfile LIFE_SEIZING_HEX = ArtifactUsageProfile.builder(
            "wanglin/life_seizing_hex", "Life Seizing Hex", "Life Seizing Hex")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Life Transformation Spell ──
    public static final ArtifactUsageProfile LIFE_TRANSFORMATION_SPELL = ArtifactUsageProfile.builder(
            "wanglin/life_transformation_spell", "Life Transformation Spell", "Life Transformation Spell")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "LifeTransformationSpell Activation", "Life Transformation Spell",
                                    "Activate the Life Transformation Spell.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Life Transformation Spell activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Light Shadow Shield ──
    public static final ArtifactUsageProfile LIGHT_SHADOW_SHIELD = ArtifactUsageProfile.builder(
            "wanglin/light_shadow_shield", "Light Shadow Shield", "Light Shadow Shield")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NASCENT_SOUL)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Lu Fu Blood Balls ──
    public static final ArtifactUsageProfile LU_FU_BLOOD_BALLS = ArtifactUsageProfile.builder(
            "wanglin/lu_fu_blood_balls", "Lu Fu Blood Balls", "Lu Fu Blood Balls")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Lu Mo Slaughter Clone ──
    public static final ArtifactUsageProfile LU_MO_SLAUGHTER_CLONE = ArtifactUsageProfile.builder(
            "wanglin/lu_mo_slaughter_clone", "Lu Mo Slaughter Clone", "Lu Mo Slaughter Clone")
            .category("Clone / Avatar")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(18.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Memory Erasing Technique ──
    public static final ArtifactUsageProfile MEMORY_ERASING_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/memory_erasing_technique", "Memory Erasing Technique", "Memory Erasing Technique")
            .category("Technique")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(20.8)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "MemoryErasingTechnique Activation", "Memory Erasing Technique",
                                    "Activate the Memory Erasing Technique.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Memory Erasing Technique activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Merit Spirit ──
    public static final ArtifactUsageProfile MERIT_SPIRIT = ArtifactUsageProfile.builder(
            "wanglin/merit_spirit", "Merit Spirit", "Merit Spirit")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Metal Essence ──
    public static final ArtifactUsageProfile METAL_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/metal_essence", "Metal Essence", "Metal Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "MetalEssence Activation", "Metal Essence",
                                    "Activate the Metal Essence.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Metal Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Miemie Essence ──
    public static final ArtifactUsageProfile MIEMIE_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/miemie_essence", "Miemie Essence", "Miemie Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "MiemieEssence Activation", "Miemie Essence",
                                    "Activate the Miemie Essence.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Miemie Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Mosquito Beast ──
    public static final ArtifactUsageProfile MOSQUITO_BEAST = ArtifactUsageProfile.builder(
            "wanglin/mosquito_beast", "Mosquito Beast", "Mosquito Beast")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Mosquito Swarm 10000 ──
    public static final ArtifactUsageProfile MOSQUITO_SWARM_10000 = ArtifactUsageProfile.builder(
            "wanglin/mosquito_swarm_10000", "Mosquito Swarm 10000", "Mosquito Swarm 10000")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Mother Child Dao Withered ──
    public static final ArtifactUsageProfile MOTHER_CHILD_DAO_WITHERED = ArtifactUsageProfile.builder(
            "wanglin/mother_child_dao_withered", "Mother Child Dao Withered", "Mother Child Dao Withered")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "MotherChildDaoWithered Activation", "Mother Child Dao Withered",
                                    "Activate the Mother Child Dao Withered.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Mother Child Dao Withered activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Mountain And River Screen ──
    public static final ArtifactUsageProfile MOUNTAIN_AND_RIVER_SCREEN = ArtifactUsageProfile.builder(
            "wanglin/mountain_and_river_screen", "Mountain And River Screen", "Mountain And River Screen")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Mountains Crumble ──
    public static final ArtifactUsageProfile MOUNTAINS_CRUMBLE = ArtifactUsageProfile.builder(
            "wanglin/mountains_crumble", "Mountains Crumble", "Mountains Crumble")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Multi Layered Illusion Spell ──
    public static final ArtifactUsageProfile MULTI_LAYERED_ILLUSION_SPELL = ArtifactUsageProfile.builder(
            "wanglin/multi_layered_illusion_spell", "Multi Layered Illusion Spell", "Multi Layered Illusion Spell")
            .category("Technique")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(20.8)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "MultiLayeredIllusionSpell Activation", "Multi Layered Illusion Spell",
                                    "Activate the Multi Layered Illusion Spell.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Multi Layered Illusion Spell activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Mysterious God Star ──
    public static final ArtifactUsageProfile MYSTERIOUS_GOD_STAR = ArtifactUsageProfile.builder(
            "wanglin/mysterious_god_star", "Mysterious God Star", "Mysterious God Star")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Nether Beast ──
    public static final ArtifactUsageProfile NETHER_BEAST = ArtifactUsageProfile.builder(
            "wanglin/nether_beast", "Nether Beast", "Nether Beast")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Nether Guide ──
    public static final ArtifactUsageProfile NETHER_GUIDE = ArtifactUsageProfile.builder(
            "wanglin/nether_guide", "Nether Guide", "Nether Guide")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Nine Cycle Celestial Refining ──
    public static final ArtifactUsageProfile NINE_CYCLE_CELESTIAL_REFINING = ArtifactUsageProfile.builder(
            "wanglin/nine_cycle_celestial_refining", "Nine Cycle Celestial Refining", "Nine Cycle Celestial Refining")
            .category("Miscellaneous")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2460.6)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Nine Drops Poison Miao Yin ──
    public static final ArtifactUsageProfile NINE_DROPS_POISON_MIAO_YIN = ArtifactUsageProfile.builder(
            "wanglin/nine_drops_poison_miao_yin", "Nine Drops Poison Miao Yin", "Nine Drops Poison Miao Yin")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Nine Mysterious Transformations ──
    public static final ArtifactUsageProfile NINE_MYSTERIOUS_TRANSFORMATIONS = ArtifactUsageProfile.builder(
            "wanglin/nine_mysterious_transformations", "Nine Mysterious Transformations", "Nine Mysterious Transformations")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "NineMysteriousTransformations Activation", "Nine Mysterious Transformations",
                                    "Activate the Nine Mysterious Transformations.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Nine Mysterious Transformations activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Nine Revolutions Refining Immortal ──
    public static final ArtifactUsageProfile NINE_REVOLUTIONS_REFINING_IMMORTAL = ArtifactUsageProfile.builder(
            "wanglin/nine_revolutions_refining_immortal", "Nine Revolutions Refining Immortal", "Nine Revolutions Refining Immortal")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Nine Star Ancient God Power ──
    public static final ArtifactUsageProfile NINE_STAR_ANCIENT_GOD_POWER = ArtifactUsageProfile.builder(
            "wanglin/nine_star_ancient_god_power", "Nine Star Ancient God Power", "Nine Star Ancient God Power")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Nine Tribulation Karma Fires ──
    public static final ArtifactUsageProfile NINE_TRIBULATION_KARMA_FIRES = ArtifactUsageProfile.builder(
            "wanglin/nine_tribulation_karma_fires", "Nine Tribulation Karma Fires", "Nine Tribulation Karma Fires")
            .category("Elemental")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1240.8)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "NineTribulationKarmaFires Activation", "Nine Tribulation Karma Fires",
                                    "Activate the Nine Tribulation Karma Fires.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Nine Tribulation Karma Fires activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── One Step Trample Heavens ──
    public static final ArtifactUsageProfile ONE_STEP_TRAMPLE_HEAVENS = ArtifactUsageProfile.builder(
            "wanglin/one_step_trample_heavens", "One Step Trample Heavens", "One Step Trample Heavens")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Open Ancient Thunder Realm ──
    public static final ArtifactUsageProfile OPEN_ANCIENT_THUNDER_REALM = ArtifactUsageProfile.builder(
            "wanglin/open_ancient_thunder_realm", "Open Ancient Thunder Realm", "Open Ancient Thunder Realm")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Pair Metal Flints ──
    public static final ArtifactUsageProfile PAIR_METAL_FLINTS = ArtifactUsageProfile.builder(
            "wanglin/pair_metal_flints", "Pair Metal Flints", "Pair Metal Flints")
            .category("Elemental")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(16.8)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "PairMetalFlints Activation", "Pair Metal Flints",
                                    "Activate the Pair Metal Flints.",
                                    0.5, 0.4, RealmId.CORE_FORMATION,
                                    "The Pair Metal Flints activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Planet Soul Extraction ──
    public static final ArtifactUsageProfile PLANET_SOUL_EXTRACTION = ArtifactUsageProfile.builder(
            "wanglin/planet_soul_extraction", "Planet Soul Extraction", "Planet Soul Extraction")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Rain Celestial Sword ──
    public static final ArtifactUsageProfile RAIN_CELESTIAL_SWORD = ArtifactUsageProfile.builder(
            "wanglin/rain_celestial_sword", "Rain Celestial Sword", "Rain Celestial Sword")
            .category("Weapon")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(12303.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, true, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "RainCelestialSword Activation", "Rain Celestial Sword",
                                    "Activate the Rain Celestial Sword.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Rain Celestial Sword activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Rapid Spell Art Xu Decai ──
    public static final ArtifactUsageProfile RAPID_SPELL_ART_XU_DECAI = ArtifactUsageProfile.builder(
            "wanglin/rapid_spell_art_xu_decai", "Rapid Spell Art Xu Decai", "Rapid Spell Art Xu Decai")
            .category("Technique")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(20.8)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "RapidSpellArtXuDecai Activation", "Rapid Spell Art Xu Decai",
                                    "Activate the Rapid Spell Art Xu Decai.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Rapid Spell Art Xu Decai activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Rapid Spell Technique ──
    public static final ArtifactUsageProfile RAPID_SPELL_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/rapid_spell_technique", "Rapid Spell Technique", "Rapid Spell Technique")
            .category("Technique")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(20.8)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "RapidSpellTechnique Activation", "Rapid Spell Technique",
                                    "Activate the Rapid Spell Technique.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Rapid Spell Technique activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Realm Defining Compass ──
    public static final ArtifactUsageProfile REALM_DEFINING_COMPASS = ArtifactUsageProfile.builder(
            "wanglin/realm_defining_compass", "Realm Defining Compass", "Realm Defining Compass")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(37.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Red Lightning Ji Realm ──
    public static final ArtifactUsageProfile RED_LIGHTNING_JI_REALM = ArtifactUsageProfile.builder(
            "wanglin/red_lightning_ji_realm", "Red Lightning Ji Realm", "Red Lightning Ji Realm")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Reincarnation Essence ──
    public static final ArtifactUsageProfile REINCARNATION_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/reincarnation_essence", "Reincarnation Essence", "Reincarnation Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1861.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_CLEANSER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ReincarnationEssence Activation", "Reincarnation Essence",
                                    "Activate the Reincarnation Essence.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The Reincarnation Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Restriction Breaking Ancient Mirror ──
    public static final ArtifactUsageProfile RESTRICTION_BREAKING_ANCIENT_MIRROR = ArtifactUsageProfile.builder(
            "wanglin/restriction_breaking_ancient_mirror", "Restriction Breaking Ancient Mirror", "Restriction Breaking Ancient Mirror")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1234.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "RestrictionBreakingAncientMirror Activation", "Restriction Breaking Ancient Mirror",
                                    "Activate the Restriction Breaking Ancient Mirror.",
                                    0.5, 0.4, RealmId.NIRVANA_FRUIT,
                                    "The Restriction Breaking Ancient Mirror activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Restriction Essence ──
    public static final ArtifactUsageProfile RESTRICTION_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/restriction_essence", "Restriction Essence", "Restriction Essence")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4921.2)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "RestrictionEssence Activation", "Restriction Essence",
                                    "Activate the Restriction Essence.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Restriction Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Restriction Flag Method ──
    public static final ArtifactUsageProfile RESTRICTION_FLAG_METHOD = ArtifactUsageProfile.builder(
            "wanglin/restriction_flag_method", "Restriction Flag Method", "Restriction Flag Method")
            .category("Flag / Banner")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(74.0)
            .attackSpeed(0.7)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Rusted Iron Sword ──
    public static final ArtifactUsageProfile RUSTED_IRON_SWORD = ArtifactUsageProfile.builder(
            "wanglin/rusted_iron_sword", "Rusted Iron Sword", "Rusted Iron Sword")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(39.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "RustedIronSword Activation", "Rusted Iron Sword",
                                    "Activate the Rusted Iron Sword.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Rusted Iron Sword activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Scattered Devil Armour ──
    public static final ArtifactUsageProfile SCATTERED_DEVIL_ARMOUR = ArtifactUsageProfile.builder(
            "wanglin/scattered_devil_armour", "Scattered Devil Armour", "Scattered Devil Armour")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NASCENT_SOUL)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Seven Colored Lance ──
    public static final ArtifactUsageProfile SEVEN_COLORED_LANCE = ArtifactUsageProfile.builder(
            "wanglin/seven_colored_lance", "Seven Colored Lance", "Seven Colored Lance")
            .category("Weapon")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(27.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SevenColoredLance Activation", "Seven Colored Lance",
                                    "Activate the Seven Colored Lance.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Seven Colored Lance activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Seven Colored Lance Dao ──
    public static final ArtifactUsageProfile SEVEN_COLORED_LANCE_DAO = ArtifactUsageProfile.builder(
            "wanglin/seven_colored_lance_dao", "Seven Colored Lance Dao", "Seven Colored Lance Dao")
            .category("Weapon")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(27.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SevenColoredLanceDao Activation", "Seven Colored Lance Dao",
                                    "Activate the Seven Colored Lance Dao.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Seven Colored Lance Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Seven Colored Lance Dao Spell ──
    public static final ArtifactUsageProfile SEVEN_COLORED_LANCE_DAO_SPELL = ArtifactUsageProfile.builder(
            "wanglin/seven_colored_lance_dao_spell", "Seven Colored Lance Dao Spell", "Seven Colored Lance Dao Spell")
            .category("Weapon")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(27.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SevenColoredLanceDaoSpell Activation", "Seven Colored Lance Dao Spell",
                                    "Activate the Seven Colored Lance Dao Spell.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Seven Colored Lance Dao Spell activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Seven Colored Nails ──
    public static final ArtifactUsageProfile SEVEN_COLORED_NAILS = ArtifactUsageProfile.builder(
            "wanglin/seven_colored_nails", "Seven Colored Nails", "Seven Colored Nails")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Seven Star Sword Formation ──
    public static final ArtifactUsageProfile SEVEN_STAR_SWORD_FORMATION = ArtifactUsageProfile.builder(
            "wanglin/seven_star_sword_formation", "Seven Star Sword Formation", "Seven Star Sword Formation")
            .category("Weapon")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(111.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SevenStarSwordFormation Activation", "Seven Star Sword Formation",
                                    "Activate the Seven Star Sword Formation.",
                                    0.5, 0.4, RealmId.ASCENDANT,
                                    "The Seven Star Sword Formation activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Short Sword Seals Shadows ──
    public static final ArtifactUsageProfile SHORT_SWORD_SEALS_SHADOWS = ArtifactUsageProfile.builder(
            "wanglin/short_sword_seals_shadows", "Short Sword Seals Shadows", "Short Sword Seals Shadows")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(39.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ShortSwordSealsShadows Activation", "Short Sword Seals Shadows",
                                    "Activate the Short Sword Seals Shadows.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Short Sword Seals Shadows activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Silver Celestial Guard Ta Shan ──
    public static final ArtifactUsageProfile SILVER_CELESTIAL_GUARD_TA_SHAN = ArtifactUsageProfile.builder(
            "wanglin/silver_celestial_guard_ta_shan", "Silver Celestial Guard Ta Shan", "Silver Celestial Guard Ta Shan")
            .category("Defense / Armor")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2460.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.TRUE_IMMORTAL)))
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Silver Celestial Guard Thunder Daoist ──
    public static final ArtifactUsageProfile SILVER_CELESTIAL_GUARD_THUNDER_DAOIST = ArtifactUsageProfile.builder(
            "wanglin/silver_celestial_guard_thunder_daoist", "Silver Celestial Guard Thunder Daoist", "Silver Celestial Guard Thunder Daoist")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SilverCelestialGuardThunderDaoist Activation", "Silver Celestial Guard Thunder Daoist",
                                    "Activate the Silver Celestial Guard Thunder Daoist.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Silver Celestial Guard Thunder Daoist activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Silver Dragon Star Compass ──
    public static final ArtifactUsageProfile SILVER_DRAGON_STAR_COMPASS = ArtifactUsageProfile.builder(
            "wanglin/silver_dragon_star_compass", "Silver Dragon Star Compass", "Silver Dragon Star Compass")
            .category("Beast / Companion")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(16404.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Silver Poison Female Corpse ──
    public static final ArtifactUsageProfile SILVER_POISON_FEMALE_CORPSE = ArtifactUsageProfile.builder(
            "wanglin/silver_poison_female_corpse", "Silver Poison Female Corpse", "Silver Poison Female Corpse")
            .category("Miscellaneous")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4.2)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Slash Luo Art ──
    public static final ArtifactUsageProfile SLASH_LUO_ART = ArtifactUsageProfile.builder(
            "wanglin/slash_luo_art", "Slash Luo Art", "Slash Luo Art")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SlashLuoArt Activation", "Slash Luo Art",
                                    "Activate the Slash Luo Art.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Slash Luo Art activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Slaughter Crystal ──
    public static final ArtifactUsageProfile SLAUGHTER_CRYSTAL = ArtifactUsageProfile.builder(
            "wanglin/slaughter_crystal", "Slaughter Crystal", "Slaughter Crystal")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1231.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Slaughter Essence ──
    public static final ArtifactUsageProfile SLAUGHTER_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/slaughter_essence", "Slaughter Essence", "Slaughter Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7390.8)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.SPIRIT_SEIZER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SlaughterEssence Activation", "Slaughter Essence",
                                    "Activate the Slaughter Essence.",
                                    0.5, 0.4, RealmId.SPIRIT_SEIZER,
                                    "The Slaughter Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .build();

    // ── Slaughter Immortal Art ──
    public static final ArtifactUsageProfile SLAUGHTER_IMMORTAL_ART = ArtifactUsageProfile.builder(
            "wanglin/slaughter_immortal_art", "Slaughter Immortal Art", "Slaughter Immortal Art")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SlaughterImmortalArt Activation", "Slaughter Immortal Art",
                                    "Activate the Slaughter Immortal Art.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Slaughter Immortal Art activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Soul Devil Ship ──
    public static final ArtifactUsageProfile SOUL_DEVIL_SHIP = ArtifactUsageProfile.builder(
            "wanglin/soul_devil_ship", "Soul Devil Ship", "Soul Devil Ship")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4116.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Soul Devourer Nature ──
    public static final ArtifactUsageProfile SOUL_DEVOURER_NATURE = ArtifactUsageProfile.builder(
            "wanglin/soul_devourer_nature", "Soul Devourer Nature", "Soul Devourer Nature")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Soul Eye Dao ──
    public static final ArtifactUsageProfile SOUL_EYE_DAO = ArtifactUsageProfile.builder(
            "wanglin/soul_eye_dao", "Soul Eye Dao", "Soul Eye Dao")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SoulEyeDao Activation", "Soul Eye Dao",
                                    "Activate the Soul Eye Dao.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Soul Eye Dao activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Soul Fantasy Origin ──
    public static final ArtifactUsageProfile SOUL_FANTASY_ORIGIN = ArtifactUsageProfile.builder(
            "wanglin/soul_fantasy_origin", "Soul Fantasy Origin", "Soul Fantasy Origin")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Soul Flag Production ──
    public static final ArtifactUsageProfile SOUL_FLAG_PRODUCTION = ArtifactUsageProfile.builder(
            "wanglin/soul_flag_production", "Soul Flag Production", "Soul Flag Production")
            .category("Flag / Banner")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(18.0)
            .attackSpeed(0.7)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Soul Flag Soul Refining Sect ──
    public static final ArtifactUsageProfile SOUL_FLAG_SOUL_REFINING_SECT = ArtifactUsageProfile.builder(
            "wanglin/soul_flag_soul_refining_sect", "Soul Flag Soul Refining Sect", "Soul Flag Soul Refining Sect")
            .category("Flag / Banner")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(18.0)
            .attackSpeed(0.7)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Soul Gourd ──
    public static final ArtifactUsageProfile SOUL_GOURD = ArtifactUsageProfile.builder(
            "wanglin/soul_gourd", "Soul Gourd", "Soul Gourd")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Soul Piercing Eyes ──
    public static final ArtifactUsageProfile SOUL_PIERCING_EYES = ArtifactUsageProfile.builder(
            "wanglin/soul_piercing_eyes", "Soul Piercing Eyes", "Soul Piercing Eyes")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Soul Searching ──
    public static final ArtifactUsageProfile SOUL_SEARCHING = ArtifactUsageProfile.builder(
            "wanglin/soul_searching", "Soul Searching", "Soul Searching")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Space Stone ──
    public static final ArtifactUsageProfile SPACE_STONE = ArtifactUsageProfile.builder(
            "wanglin/space_stone", "Space Stone", "Space Stone")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.QI_CONDENSATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.5)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.QI_CONDENSATION)
            .build();

    // ── Spatial Bending ──
    public static final ArtifactUsageProfile SPATIAL_BENDING = ArtifactUsageProfile.builder(
            "wanglin/spatial_bending", "Spatial Bending", "Spatial Bending")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Spirit Transformation ──
    public static final ArtifactUsageProfile SPIRIT_TRANSFORMATION = ArtifactUsageProfile.builder(
            "wanglin/spirit_transformation", "Spirit Transformation", "Spirit Transformation")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SpiritTransformation Activation", "Spirit Transformation",
                                    "Activate the Spirit Transformation.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Spirit Transformation activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Star Compass ──
    public static final ArtifactUsageProfile STAR_COMPASS = ArtifactUsageProfile.builder(
            "wanglin/star_compass", "Star Compass", "Star Compass")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4101.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Star Of Law ──
    public static final ArtifactUsageProfile STAR_OF_LAW = ArtifactUsageProfile.builder(
            "wanglin/star_of_law", "Star Of Law", "Star Of Law")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Star Rotation ──
    public static final ArtifactUsageProfile STAR_ROTATION = ArtifactUsageProfile.builder(
            "wanglin/star_rotation", "Star Rotation", "Star Rotation")
            .category("Miscellaneous")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2460.6)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Store All Ji Thunder ──
    public static final ArtifactUsageProfile STORE_ALL_JI_THUNDER = ArtifactUsageProfile.builder(
            "wanglin/store_all_ji_thunder", "Store All Ji Thunder", "Store All Ji Thunder")
            .category("Elemental")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(21.6)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "StoreAllJiThunder Activation", "Store All Ji Thunder",
                                    "Activate the Store All Ji Thunder.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Store All Ji Thunder activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Straw Hat ──
    public static final ArtifactUsageProfile STRAW_HAT = ArtifactUsageProfile.builder(
            "wanglin/straw_hat", "Straw Hat", "Straw Hat")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Sundered Night ──
    public static final ArtifactUsageProfile SUNDERED_NIGHT = ArtifactUsageProfile.builder(
            "wanglin/sundered_night", "Sundered Night", "Sundered Night")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Sword Teleportation Spell ──
    public static final ArtifactUsageProfile SWORD_TELEPORTATION_SPELL = ArtifactUsageProfile.builder(
            "wanglin/sword_teleportation_spell", "Sword Teleportation Spell", "Sword Teleportation Spell")
            .category("Weapon")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(111.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "SwordTeleportationSpell Activation", "Sword Teleportation Spell",
                                    "Activate the Sword Teleportation Spell.",
                                    0.5, 0.4, RealmId.ASCENDANT,
                                    "The Sword Teleportation Spell activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Taichu Essence ──
    public static final ArtifactUsageProfile TAICHU_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/taichu_essence", "Taichu Essence", "Taichu Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14763.6)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.TRUE_IMMORTAL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "TaichuEssence Activation", "Taichu Essence",
                                    "Activate the Taichu Essence.",
                                    0.5, 0.4, RealmId.TRUE_IMMORTAL,
                                    "The Taichu Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Tattoo Talisman Speed Boost ──
    public static final ArtifactUsageProfile TATTOO_TALISMAN_SPEED_BOOST = ArtifactUsageProfile.builder(
            "wanglin/tattoo_talisman_speed_boost", "Tattoo Talisman Speed Boost", "Tattoo Talisman Speed Boost")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Teleportation Restriction ──
    public static final ArtifactUsageProfile TELEPORTATION_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/teleportation_restriction", "Teleportation Restriction", "Teleportation Restriction")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "TeleportationRestriction Activation", "Teleportation Restriction",
                                    "Activate the Teleportation Restriction.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Teleportation Restriction activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Ten Million Swords ──
    public static final ArtifactUsageProfile TEN_MILLION_SWORDS = ArtifactUsageProfile.builder(
            "wanglin/ten_million_swords", "Ten Million Swords", "Ten Million Swords")
            .category("Weapon")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(27.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "TenMillionSwords Activation", "Ten Million Swords",
                                    "Activate the Ten Million Swords.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Ten Million Swords activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Third Eye Spell ──
    public static final ArtifactUsageProfile THIRD_EYE_SPELL = ArtifactUsageProfile.builder(
            "wanglin/third_eye_spell", "Third Eye Spell", "Third Eye Spell")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(13.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Three Battle Scrolls Zhan ──
    public static final ArtifactUsageProfile THREE_BATTLE_SCROLLS_ZHAN = ArtifactUsageProfile.builder(
            "wanglin/three_battle_scrolls_zhan", "Three Battle Scrolls Zhan", "Three Battle Scrolls Zhan")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(13.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Three Bells Shield ──
    public static final ArtifactUsageProfile THREE_BELLS_SHIELD = ArtifactUsageProfile.builder(
            "wanglin/three_bells_shield", "Three Bells Shield", "Three Bells Shield")
            .category("Defense / Armor")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(22.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.ASCENDANT)))
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Three Ink Stones Restriction ──
    public static final ArtifactUsageProfile THREE_INK_STONES_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/three_ink_stones_restriction", "Three Ink Stones Restriction", "Three Ink Stones Restriction")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(44.4)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ThreeInkStonesRestriction Activation", "Three Ink Stones Restriction",
                                    "Activate the Three Ink Stones Restriction.",
                                    0.5, 0.4, RealmId.ASCENDANT,
                                    "The Three Ink Stones Restriction activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.ASCENDANT)
            .build();

    // ── Three Life Spell ──
    public static final ArtifactUsageProfile THREE_LIFE_SPELL = ArtifactUsageProfile.builder(
            "wanglin/three_life_spell", "Three Life Spell", "Three Life Spell")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ThreeLifeSpell Activation", "Three Life Spell",
                                    "Activate the Three Life Spell.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Three Life Spell activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Three Life Spell William ──
    public static final ArtifactUsageProfile THREE_LIFE_SPELL_WILLIAM = ArtifactUsageProfile.builder(
            "wanglin/three_life_spell_william", "Three Life Spell William", "Three Life Spell William")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ThreeLifeSpellWilliam Activation", "Three Life Spell William",
                                    "Activate the Three Life Spell William.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Three Life Spell William activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Three Purple Flags Defensive ──
    public static final ArtifactUsageProfile THREE_PURPLE_FLAGS_DEFENSIVE = ArtifactUsageProfile.builder(
            "wanglin/three_purple_flags_defensive", "Three Purple Flags Defensive", "Three Purple Flags Defensive")
            .category("Flag / Banner")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.0)
            .attackSpeed(0.7)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── Thunder Body ──
    public static final ArtifactUsageProfile THUNDER_BODY = ArtifactUsageProfile.builder(
            "wanglin/thunder_body", "Thunder Body", "Thunder Body")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NASCENT_SOUL)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Thunder Celestial Beast ──
    public static final ArtifactUsageProfile THUNDER_CELESTIAL_BEAST = ArtifactUsageProfile.builder(
            "wanglin/thunder_celestial_beast", "Thunder Celestial Beast", "Thunder Celestial Beast")
            .category("Beast / Companion")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(16404.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .build();

    // ── Thunder Essence ──
    public static final ArtifactUsageProfile THUNDER_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/thunder_essence", "Thunder Essence", "Thunder Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ThunderEssence Activation", "Thunder Essence",
                                    "Activate the Thunder Essence.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Thunder Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Thunder Origin Spell ──
    public static final ArtifactUsageProfile THUNDER_ORIGIN_SPELL = ArtifactUsageProfile.builder(
            "wanglin/thunder_origin_spell", "Thunder Origin Spell", "Thunder Origin Spell")
            .category("Technique")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(20.8)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ThunderOriginSpell Activation", "Thunder Origin Spell",
                                    "Activate the Thunder Origin Spell.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Thunder Origin Spell activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Thunder Toad ──
    public static final ArtifactUsageProfile THUNDER_TOAD = ArtifactUsageProfile.builder(
            "wanglin/thunder_toad", "Thunder Toad", "Thunder Toad")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Time Restriction ──
    public static final ArtifactUsageProfile TIME_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/time_restriction", "Time Restriction", "Time Restriction")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "TimeRestriction Activation", "Time Restriction",
                                    "Activate the Time Restriction.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Time Restriction activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Tortoise Shell ──
    public static final ArtifactUsageProfile TORTOISE_SHELL = ArtifactUsageProfile.builder(
            "wanglin/tortoise_shell", "Tortoise Shell", "Tortoise Shell")
            .category("Defense / Armor")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(4.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.CORE_FORMATION)))
            .authorityRealm(RealmId.CORE_FORMATION)
            .build();

    // ── True False Domain ──
    public static final ArtifactUsageProfile TRUE_FALSE_DOMAIN = ArtifactUsageProfile.builder(
            "wanglin/true_false_domain", "True False Domain", "True False Domain")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(310.2)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── True False Essence ──
    public static final ArtifactUsageProfile TRUE_FALSE_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/true_false_essence", "True False Essence", "True False Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1861.2)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NIRVANA_CLEANSER)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "TrueFalseEssence Activation", "True False Essence",
                                    "Activate the True False Essence.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The True False Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── True False Eternal Seal ──
    public static final ArtifactUsageProfile TRUE_FALSE_ETERNAL_SEAL = ArtifactUsageProfile.builder(
            "wanglin/true_false_eternal_seal", "True False Eternal Seal", "True False Eternal Seal")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NIRVANA_CLEANSER)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(620.4)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "TrueFalseEternalSeal Activation", "True False Eternal Seal",
                                    "Activate the True False Eternal Seal.",
                                    0.5, 0.4, RealmId.NIRVANA_CLEANSER,
                                    "The True False Eternal Seal activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_CLEANSER)
            .build();

    // ── Underworld Ascension Method ──
    public static final ArtifactUsageProfile UNDERWORLD_ASCENSION_METHOD = ArtifactUsageProfile.builder(
            "wanglin/underworld_ascension_method", "Underworld Ascension Method", "Underworld Ascension Method")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "UnderworldAscensionMethod Activation", "Underworld Ascension Method",
                                    "Activate the Underworld Ascension Method.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Underworld Ascension Method activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Underworld Finger ──
    public static final ArtifactUsageProfile UNDERWORLD_FINGER = ArtifactUsageProfile.builder(
            "wanglin/underworld_finger", "Underworld Finger", "Underworld Finger")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Undying Ancient Body ──
    public static final ArtifactUsageProfile UNDYING_ANCIENT_BODY = ArtifactUsageProfile.builder(
            "wanglin/undying_ancient_body", "Undying Ancient Body", "Undying Ancient Body")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NIRVANA_FRUIT)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Undying Ancient Body Version ──
    public static final ArtifactUsageProfile UNDYING_ANCIENT_BODY_VERSION = ArtifactUsageProfile.builder(
            "wanglin/undying_ancient_body_version", "Undying Ancient Body Version", "Undying Ancient Body Version")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(617.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NIRVANA_FRUIT)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Undying Ancient Finger ──
    public static final ArtifactUsageProfile UNDYING_ANCIENT_FINGER = ArtifactUsageProfile.builder(
            "wanglin/undying_ancient_finger", "Undying Ancient Finger", "Undying Ancient Finger")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Unnamed Wheel Formation ──
    public static final ArtifactUsageProfile UNNAMED_WHEEL_FORMATION = ArtifactUsageProfile.builder(
            "wanglin/unnamed_wheel_formation", "Unnamed Wheel Formation", "Unnamed Wheel Formation")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(10.8)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "UnnamedWheelFormation Activation", "Unnamed Wheel Formation",
                                    "Activate the Unnamed Wheel Formation.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Unnamed Wheel Formation activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Vermilion Bird Feather ──
    public static final ArtifactUsageProfile VERMILION_BIRD_FEATHER = ArtifactUsageProfile.builder(
            "wanglin/vermilion_bird_feather", "Vermilion Bird Feather", "Vermilion Bird Feather")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Vermilion Bird Holy Token ──
    public static final ArtifactUsageProfile VERMILION_BIRD_HOLY_TOKEN = ArtifactUsageProfile.builder(
            "wanglin/vermilion_bird_holy_token", "Vermilion Bird Holy Token", "Vermilion Bird Holy Token")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Vermilion Bird Spirit ──
    public static final ArtifactUsageProfile VERMILION_BIRD_SPIRIT = ArtifactUsageProfile.builder(
            "wanglin/vermilion_bird_spirit", "Vermilion Bird Spirit", "Vermilion Bird Spirit")
            .category("Miscellaneous")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Vice Ghostly Sail ──
    public static final ArtifactUsageProfile VICE_GHOSTLY_SAIL = ArtifactUsageProfile.builder(
            "wanglin/vice_ghostly_sail", "Vice Ghostly Sail", "Vice Ghostly Sail")
            .category("Elemental")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(2469.6)
            .attackSpeed(0.8)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "ViceGhostlySail Activation", "Vice Ghostly Sail",
                                    "Activate the Vice Ghostly Sail.",
                                    0.5, 0.4, RealmId.NIRVANA_FRUIT,
                                    "The Vice Ghostly Sail activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── Void Avatar ──
    public static final ArtifactUsageProfile VOID_AVATAR = ArtifactUsageProfile.builder(
            "wanglin/void_avatar", "Void Avatar", "Void Avatar")
            .category("Clone / Avatar")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(18.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Void Spell 8 Star ──
    public static final ArtifactUsageProfile VOID_SPELL_8_STAR = ArtifactUsageProfile.builder(
            "wanglin/void_spell_8_star", "Void Spell 8 Star", "Void Spell 8 Star")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "VoidSpell8Star Activation", "Void Spell 8 Star",
                                    "Activate the Void Spell 8 Star.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Void Spell 8 Star activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Void Stop Spell ──
    public static final ArtifactUsageProfile VOID_STOP_SPELL = ArtifactUsageProfile.builder(
            "wanglin/void_stop_spell", "Void Stop Spell", "Void Stop Spell")
            .category("Technique")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(14.4)
            .attackSpeed(1.2)
            .passiveEffects(List.of())
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "VoidStopSpell Activation", "Void Stop Spell",
                                    "Activate the Void Stop Spell.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Void Stop Spell activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Wandering Souls ──
    public static final ArtifactUsageProfile WANDERING_SOULS = ArtifactUsageProfile.builder(
            "wanglin/wandering_souls", "Wandering Souls", "Wandering Souls")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(1029.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .build();

    // ── War Spirit Print ──
    public static final ArtifactUsageProfile WAR_SPIRIT_PRINT = ArtifactUsageProfile.builder(
            "wanglin/war_spirit_print", "War Spirit Print", "War Spirit Print")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Water Essence ──
    public static final ArtifactUsageProfile WATER_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/water_essence", "Water Essence", "Water Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "WaterEssence Activation", "Water Essence",
                                    "Activate the Water Essence.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Water Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Wealth Flying Sword ──
    public static final ArtifactUsageProfile WEALTH_FLYING_SWORD = ArtifactUsageProfile.builder(
            "wanglin/wealth_flying_sword", "Wealth Flying Sword", "Wealth Flying Sword")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(39.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "WealthFlyingSword Activation", "Wealth Flying Sword",
                                    "Activate the Wealth Flying Sword.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Wealth Flying Sword activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── White Hair Strand ──
    public static final ArtifactUsageProfile WHITE_HAIR_STRAND = ArtifactUsageProfile.builder(
            "wanglin/white_hair_strand", "White Hair Strand", "White Hair Strand")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(9.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Wither Dao Pair ──
    public static final ArtifactUsageProfile WITHER_DAO_PAIR = ArtifactUsageProfile.builder(
            "wanglin/wither_dao_pair", "Wither Dao Pair", "Wither Dao Pair")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "WitherDaoPair Activation", "Wither Dao Pair",
                                    "Activate the Wither Dao Pair.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Wither Dao Pair activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Wood Carving Black Fiend ──
    public static final ArtifactUsageProfile WOOD_CARVING_BLACK_FIEND = ArtifactUsageProfile.builder(
            "wanglin/wood_carving_black_fiend", "Wood Carving Black Fiend", "Wood Carving Black Fiend")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Wood Essence ──
    public static final ArtifactUsageProfile WOOD_ESSENCE = ArtifactUsageProfile.builder(
            "wanglin/wood_essence", "Wood Essence", "Wood Essence")
            .category("Dao Essence")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(32.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "WoodEssence Activation", "Wood Essence",
                                    "Activate the Wood Essence.",
                                    0.5, 0.4, RealmId.NASCENT_SOUL,
                                    "The Wood Essence activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Xie Qing Immortal Guard ──
    public static final ArtifactUsageProfile XIE_QING_IMMORTAL_GUARD = ArtifactUsageProfile.builder(
            "wanglin/xie_qing_immortal_guard", "Xie Qing Immortal Guard", "Xie Qing Immortal Guard")
            .category("Defense / Armor")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(5.4)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.NASCENT_SOUL)))
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Xu Liguo Devil ──
    public static final ArtifactUsageProfile XU_LIGUO_DEVIL = ArtifactUsageProfile.builder(
            "wanglin/xu_liguo_devil", "Xu Liguo Devil", "Xu Liguo Devil")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(36.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Yi Si Puppet ──
    public static final ArtifactUsageProfile YI_SI_PUPPET = ArtifactUsageProfile.builder(
            "wanglin/yi_si_puppet", "Yi Si Puppet", "Yi Si Puppet")
            .category("Clone / Avatar")
            .artifactRealm(RealmId.NASCENT_SOUL)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(18.0)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.NASCENT_SOUL)
            .build();

    // ── Yin Blade ──
    public static final ArtifactUsageProfile YIN_BLADE = ArtifactUsageProfile.builder(
            "wanglin/yin_blade", "Yin Blade", "Yin Blade")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(39.0)
            .attackSpeed(0.9)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL)))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "YinBlade Activation", "Yin Blade",
                                    "Activate the Yin Blade.",
                                    0.5, 0.4, RealmId.SOUL_FORMATION,
                                    "The Yin Blade activates with a pulse of power."
                            )))
            )
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Yin Energy Detection ──
    public static final ArtifactUsageProfile YIN_ENERGY_DETECTION = ArtifactUsageProfile.builder(
            "wanglin/yin_energy_detection", "Yin Energy Detection", "Yin Energy Detection")
            .category("Miscellaneous")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))
            .baseDamage(7.8)
            .attackSpeed(1.0)
            .passiveEffects(List.of())
            .authorityRealm(RealmId.SOUL_FORMATION)
            .build();

    // ── Bootstrap ──────────────────────────────────────────

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        // All profiles are static final fields — no dynamic registry needed.
        // Total generated profiles: 300
    }
}