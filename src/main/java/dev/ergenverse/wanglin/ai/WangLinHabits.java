package dev.ergenverse.wanglin.ai;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * WangLinHabits — Wang Lin's reflexive BEHAVIOURAL habits, each with a {@link Provenance}.
 *
 * <p>Traits describe what Wang Lin <i>is</i>; habits describe what he <i>does
 * reflexively</i> — the small, repeated actions that define his daily life as a
 * cultivator. An NPC AI that knows the habits can produce canon-faithful
 * micro-behaviour: scoping a room with divine sense before stepping in,
 * palming an escape talisman, retreating to the bead to cultivate under time
 * dilation, maintaining his puppets at dusk.
 *
 * <p>Per the Prime Directive: every habit is canon-attested. Er Gen wrote the
 * behaviour (often repeatedly, across many chapters); the habit name is the
 * simulation's generalization. Where a habit is expressed only through
 * consistent action rather than narrator statement, attestation is INFERRED
 * with confidence 4 and an ambiguity note explaining the derivation.
 *
 * <h2>Habit inventory (11 habits)</h2>
 * <ol>
 *   <li><b>DIVINE_SENSE_SCOUTING</b> — scans every new environment with
 *       divine sense before entering.</li>
 *   <li><b>ESCAPE_TALISMAN_READINESS</b> — always keeps a teleportation /
 *       earth-escape route prepared.</li>
 *   <li><b>RESOURCE_HOARDING</b> — collects every material, soul, and
 *       fragment; wastes nothing.</li>
 *   <li><b>PUPPET_IMMORTAL_GUARD_MAINTENANCE</b> — tends his puppet army
 *       and Immortal Guards as a nightly discipline.</li>
 *   <li><b>RESTRICTION_PRACTICE</b> — daily restriction study; the
 *       Illusionary Circle meditation.</li>
 *   <li><b>BEAD_TIME_DILATION_CULTIVATION</b> — retreats into the bead's
 *       10× time chamber for deep cultivation.</li>
 *   <li><b>CULTIVATION_LEVEL_DISGUISE</b> — habitually conceals his true
 *       realm; reveals it only when necessary.</li>
 *   <li><b>INTENTION_TESTING</b> — tests the intentions of strangers
 *       through indirect provocation before trusting them.</li>
 *   <li><b>ENEMY_SOUL_STORAGE</b> — stores the souls of slain enemies in
 *       the Soul Lasher / Karma Whip for later use or torment.</li>
 *   <li><b>MORTAL_LIFE_COMPREHENSION</b> — periodically lives as a mortal
 *       to comprehend Life-Death and Samsara.</li>
 *   <li><b>AVATAR_MAIN_BODY_SPLIT</b> — operates through avatars (the
 *       Big-Headed Elder, the Slaughter-clone) while the main body stays
 *       safe.</li>
 * </ol>
 *
 * <h2>How the AI uses these habits</h2>
 * <p>A behaviour scheduler (future, Layer 2) consults the habit list to decide
 * what Wang Lin does in idle moments. When the player visits Wang Lin's
 * manifestation, he may be found maintaining a puppet, studying a restriction
 * scroll, or cultivating in the bead — depending on the time of day and his
 * current goal. Each habit links to the trait it reinforces via
 * {@link #drivesTrait()}, so the AI can explain <i>why</i> he is doing it.
 */
public final class WangLinHabits {

    private WangLinHabits() {}

    /**
     * A single Wang Lin behavioural habit.
     *
     * @param habitId       stable identifier (e.g. {@code "DIVINE_SENSE_SCOUTING"})
     * @param name          display name
     * @param description   one-paragraph canon-grounded description
     * @param frequencyNote how often / under what trigger the habit fires
     *                      (e.g. "every new location", "nightly at dusk")
     * @param drivesTrait   the traitId (see {@link WangLinTraits}) this habit
     *                      reinforces; may be empty if not directly linked
     * @param provenance    source novel, chapters, attestation, confidence,
     *                      ambiguities
     */
    public record Habit(
            String habitId,
            String name,
            String description,
            String frequencyNote,
            String drivesTrait,
            Provenance provenance
    ) {
        public Habit {
            if (habitId == null || habitId.isBlank()) {
                throw new IllegalArgumentException("Habit requires a habitId");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Habit '" + habitId + "' requires a name");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Habit '" + habitId + "' requires a description");
            }
            if (provenance == null) {
                throw new IllegalArgumentException("Habit '" + habitId + "' requires a Provenance");
            }
            if (frequencyNote == null) frequencyNote = "";
            if (drivesTrait == null) drivesTrait = "";
        }
    }

    // ───────────────────────────────────────────────────────────────────
    //  THE 11 CANON HABITS
    // ───────────────────────────────────────────────────────────────────

    /**
     * DIVINE_SENSE_SCOUTING — Wang Lin's reflexive first action in any new
     * environment. He extends divine sense to map the terrain, detect
     * cultivators, identify threats, and locate points of interest BEFORE
     * his physical body enters. Canon: this is his most-repeated behaviour
     * across the entire novel — from the Heng Yue Sect (Ch. 12) through the
     * Immortal Astral Continent.
     */
    public static final Habit DIVINE_SENSE_SCOUTING = new Habit(
            "DIVINE_SENSE_SCOUTING",
            "Divine-Sense Scouting — Maps Every New Environment Before Entering",
            "Wang Lin's signature recon habit. Before stepping into any new location "
                    + "— a sect, a battlefield, a market, a stranger's cave — he extends divine "
                    + "sense to map the terrain, read the cultivation levels of those present, "
                    + "detect concealed formations, and locate escape routes. This is the "
                    + "behavioural expression of EXTREME_CAUTION: he never walks into a room "
                    + "he has not already seen with his spirit.",
            "Every new location; before any physical entry.",
            "EXTREME_CAUTION",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 12 (Heng Yue Sect entry)", "Ch. 134 (Earth Escape)",
                            "Ch. 493 (Teleportation Restriction)", "Ch. 754 (Annihilation Restriction scoping)"),
                    5,
                    "Repeatedly attested across every arc; arguably his single most-consistent behaviour.")
    );

    /**
     * ESCAPE_TALISMAN_READINESS — Wang Lin always keeps a teleportation
     * restriction or earth-escape technique prepared and ready to trigger.
     * Canon: he developed the Earth Escape (Ch. 134) and later the
     * Teleportation Restriction (Ch. 493) specifically for this purpose.
     */
    public static final Habit ESCAPE_TALISMAN_READINESS = new Habit(
            "ESCAPE_TALISMAN_READINESS",
            "Escape-Talisman Readiness — Always Keeps a Way Out Prepared",
            "Wang Lin never commits to a confrontation without a prepared escape. He "
                    + "developed the Earth Escape technique (Ch. 134) early in the Sea of Devils, "
                    + "later refined it into the Teleportation Restriction (Ch. 493), and habitually "
                    + "kept one charged at all times. Even when overwhelming victory seemed certain, "
                    + "he maintained the escape route — because his life has taught him that "
                    + "overconfidence kills.",
            "Continuous; refreshed after every use.",
            "EXTREME_CAUTION",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 134 (Earth Escape)", "Ch. 493 (Teleportation Restriction)"),
                    5,
                    "Canon-attested development of both techniques specifically for escape readiness.")
    );

    /**
     * RESOURCE_HOARDING — Wang Lin collects every material, soul, and
     * fragment he encounters. Nothing is wasted. He stores beast cores,
     * spirit herbs, soul fragments, restriction materials, and even the
     * souls of slain enemies. Canon: the Soul Lasher / Karma Whip and the
     * Heaven-Defying Bead's storage are both filled with hoarded resources.
     */
    public static final Habit RESOURCE_HOARDING = new Habit(
            "RESOURCE_HOARDING",
            "Resource Hoarding — Collects Every Material, Soul, and Fragment; Wastes Nothing",
            "Wang Lin's survivor's instinct from his impoverished mortal origins and the "
                    + "Heng Yue Sect's resource scarcity. He hoards every material — beast cores, "
                    + "spirit herbs, soul fragments, restriction materials, even the souls of slain "
                    + "enemies. The Heaven-Defying Bead's storage vaults and the Soul Lasher's soul "
                    + "reservoir are both testaments to this habit. He will pause mid-combat to "
                    + "collect a valuable material if the tactical situation permits.",
            "Continuous; triggered by encountering any usable resource.",
            "PRAGMATISM_OVER_PRIDE",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 12-50 (Heng Yue Sect resource scarcity arc)",
                            "Ch. 86 (Foundation Stealing — collects the foundation)",
                            "E14 (Teng Clan — collects enemy souls)"),
                    4,
                    "Attested through consistent action across the novel; Er Gen never narrates "
                            + "'Wang Lin hoards resources' as a single statement, but the behaviour is "
                            + "pervasive. Cross-referenced CANON_RI_COMPLETE_ITEMS.md.")
    );

    /**
     * PUPPET_IMMORTAL_GUARD_MAINTENANCE — Wang Lin tends his puppet army
     * and the Immortal Guards as a nightly discipline. Canon: he created
     * the Immortal Guards from the Foreign Battleground's soul-steel; he
     * refined a puppet army that grew to thousands.
     */
    public static final Habit PUPPET_IMMORTAL_GUARD_MAINTENANCE = new Habit(
            "PUPPET_IMMORTAL_GUARD_MAINTENANCE",
            "Puppet & Immortal-Guard Maintenance — Tends His Construct Army Nightly",
            "Wang Lin maintains a standing army of puppets and Immortal Guards — soul-forged "
                    + "constructs that serve as his vanguard, his labour force, and his traps. "
                    + "He refines them in batches, repairs battle damage, and upgrades their core "
                    + "materials. This is both a martial discipline and a meditative one: the "
                    + "refinement work deepens his understanding of soul, restriction, and material.",
            "Nightly; intensified before any planned engagement.",
            "RESTRICTION_OBSESSION",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 400+ (Foreign Battleground — Immortal Guard creation)",
                            "E24 (Thunder Celestial Tournament — puppet army deployed)"),
                    4,
                    "Canon-attested creation and deployment; nightly maintenance inferred from "
                            + "the scale of the army and his meticulous nature.")
    );

    /**
     * RESTRICTION_PRACTICE — Wang Lin studies restrictions daily. He
     * developed the Illusionary Circle technique (Ch. 180) — wave-based
     * restriction analysis without needing to see the restriction. His 7
     * years at Restriction Mountain (E43) made this a lifelong discipline.
     */
    public static final Habit RESTRICTION_PRACTICE = new Habit(
            "RESTRICTION_PRACTICE",
            "Restriction Practice — Daily Study of Restrictions; the Illusionary Circle Meditation",
            "Wang Lin's defining scholarly habit. He studies restrictions daily — analyzing "
                    + "recorded restriction patterns, practising the Illusionary Circle technique "
                    + "(Ch. 180) that lets him read restrictions through wave resonance without "
                    + "direct sight, and refining his own restriction designs. The 7 years at "
                    + "Restriction Mountain (E43) forged this into a lifelong discipline; his hair "
                    + "turned white there, and the habit never left him.",
            "Daily; the Illusionary Circle meditation at dawn.",
            "RESTRICTION_OBSESSION",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 179-180 (Restriction Mountain trial, Illusionary Circle)",
                            "E43 (7-year trial)", "Ch. 754 (Annihilation Restriction)",
                            "Ch. 1715 (Restriction Essence)"),
                    5,
                    "Explicitly attested as a multi-year trial and a recurring practice throughout.")
    );

    /**
     * BEAD_TIME_DILATION_CULTIVATION — Wang Lin retreats into the
     * Heaven-Defying Bead's interior to cultivate under 10× time dilation.
     * Canon: the bead's interior time flows 10× faster than the outside.
     */
    public static final Habit BEAD_TIME_DILATION_CULTIVATION = new Habit(
            "BEAD_TIME_DILATION_CULTIVATION",
            "Bead Time-Dilation Cultivation — Retreats Into the Bead's 10× Time Chamber",
            "Wang Lin's primary cultivation-acceleration habit. The Heaven-Defying Bead's "
                    + "interior time flows 10× faster than the outside world: 1 hour outside = 10 "
                    + "hours inside. He retreats into the bead whenever he needs extended "
                    + "cultivation time without losing position in the outside world — breaking a "
                    + "bottleneck, comprehending a new technique, refining a treasure. This is how "
                    + "he kept pace with geniuses who had centuries on him.",
            "Whenever a cultivation bottleneck or comprehension opportunity arises.",
            "PATIENCE",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 8 (bead origin)", "Ch. 50+ (interior time dilation discovered)",
                            "Ch. 1433 (the '2,000 years alone' passage — bead time)"),
                    5,
                    "The 10× time dilation is canon-explicit; his habitual use of it is attested "
                            + "across multiple breakthrough arcs.")
    );

    /**
     * CULTIVATION_LEVEL_DISGUISE — Wang Lin habitually conceals his true
     * cultivation realm, revealing it only when tactically necessary.
     * Canon: he repeatedly disguised himself as a Foundation establishment
     * or Core Formation cultivator while actually being Nascent Soul or
     * higher.
     */
    public static final Habit CULTIVATION_LEVEL_DISGUISE = new Habit(
            "CULTIVATION_LEVEL_DISGUISE",
            "Cultivation-Level Disguise — Habitually Conceals His True Realm",
            "Wang Lin's signature social-camouflage habit. He habitually suppresses and "
                    + "disguises his cultivation aura, appearing as a Foundation Establishment or "
                    + "Core Formation cultivator while actually being Nascent Soul, Soul Formation, "
                    + "or higher. He reveals his true realm only when it serves a tactical purpose — "
                    + "to intimidate, to test, or to end a fight instantly. This habit let him "
                    + "survive the Sea of Devils, the Foreign Battleground, and the Thunder "
                    + "Celestial Tournament despite being hunted.",
            "Continuous; the disguise is his default public state.",
            "PRAGMATISM_OVER_PRIDE",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 180+ (Illusionary Circle — aura disguise)",
                            "E15-E22 (Sea of Devils — disguised cultivation)",
                            "E24 (Thunder Celestial Tournament — concealed realm)"),
                    5,
                    "Explicitly attested as a recurring tactic; arguably his most-used survival skill.")
    );

    /**
     * INTENTION_TESTING — Wang Lin tests the intentions of strangers
     * through indirect provocation before trusting them. Canon: his early
     * encounters with Situ Nan, Zhou Yi, and others follow this pattern.
     */
    public static final Habit INTENTION_TESTING = new Habit(
            "INTENTION_TESTING",
            "Intention Testing — Probes Strangers Indirectly Before Trusting Them",
            "Wang Lin's social-vetting habit. When he encounters a stranger whose intentions "
                    + "are unknown, he does not ask directly. He creates a small provocation — a "
                    + "deliberate reveal of false weakness, a feigned interest in a trivial matter, "
                    + "a deliberately ambiguous response — and observes how the stranger reacts. "
                    + "Greed, cruelty, impatience, or dishonesty revealed in this probe permanently "
                    + "marks the stranger as untrusted. Restraint, curiosity, or protectiveness "
                    + "earns a cautious opening. This is how he vetted Situ Nan, Zhou Yi, and later "
                    + "Lu Yun.",
            "On first meaningful encounter with any unknown cultivator.",
            "EXTREME_CAUTION",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 50+ (Situ Nan encounter — indirect vetting)",
                            "E15 (Zhou Yi encounter)", "E29 (Lu Yun encounter)"),
                    4,
                    "Attested through consistent interaction patterns; Er Gen does not name the "
                            + "habit but the probing behaviour is recognizable across his meetings.")
    );

    /**
     * ENEMY_SOUL_STORAGE — Wang Lin stores the souls of slain enemies in
     * the Soul Lasher (later the Karma Whip) for later use or torment.
     * Canon: the Soul Lasher is a soul-storage flag; he filled it with the
     * souls of the Teng Clan and many others.
     */
    public static final Habit ENEMY_SOUL_STORAGE = new Habit(
            "ENEMY_SOUL_STORAGE",
            "Enemy-Soul Storage — Stores Slain Enemy Souls in the Soul Lasher / Karma Whip",
            "Wang Lin's grim post-combat habit. When he kills an enemy — especially one who "
                    + "wronged him or his — he does not let the soul disperse. He captures it into "
                    + "the Soul Lasher (later reforged as the Karma Whip, Ch. 731), where the soul "
                    + "is stored for later use (soul refinement, restriction fuel, interrogation) "
                    + "or torment (the Teng Clan elders' souls were kept as perpetual punishment). "
                    + "This is the behavioural expression of VENGEFULNESS: the grudge does not end "
                    + "with death.",
            "After every significant enemy kill; always for lineage-extermination targets.",
            "VENGEFULNESS",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 86 (Foundation Stealing — soul capture)",
                            "E14 (Teng Clan — souls stored in Soul Lasher)",
                            "Ch. 731 (Soul Lasher → Karma Whip reforging)"),
                    5,
                    "Explicitly attested; the Soul Lasher / Karma Whip's entire function is "
                            + "soul storage, and he uses it consistently.")
    );

    /**
     * MORTAL_LIFE_COMPREHENSION — Wang Lin periodically lives as a mortal
     * to comprehend Life-Death and Samsara. Canon: his mortal lifetime
     * raising Wang Ping (E50, Ch. 701) is the most-attested instance.
     */
    public static final Habit MORTAL_LIFE_COMPREHENSION = new Habit(
            "MORTAL_LIFE_COMPREHENSION",
            "Mortal-Life Comprehension — Periodically Lives as a Mortal to Comprehend Life-Death",
            "Wang Lin's Dao-deepening habit. To comprehend Life-Death — the bridge between "
                    + "mortality and immortality, and the foundation of his Samsara Dao — he "
                    + "periodically sheds his cultivation identity and lives as a mortal. The most "
                    + "attested instance is the mortal lifetime raising his adopted son Wang Ping in "
                    + "a desolate village (E50, Ch. 701). He worked the soil, aged with his family, "
                    + "watched his son grow old and die. Each mortal lifetime deepens his grasp of "
                    + "the Life-Death restriction and the Samsara Essence.",
            "Rare; undertaken at Dao-bottleneck moments, typically lasting a mortal lifetime.",
            "MORTAL_WORLD_ATTACHMENT",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 701 (Wang Ping mortal life)", "E50",
                            "Ch. 1229 (Life-Death Restriction)"),
                    5,
                    "Explicitly attested as a named Dao-comprehension practice; the Wang Ping "
                            + "lifetime is one of the novel's most-cited arcs.")
    );

    /**
     * AVATAR_MAIN_BODY_SPLIT — Wang Lin operates through avatars (the
     * Big-Headed Elder, the Slaughter-clone) while the main body stays
     * safe. Canon: he developed multiple avatars to project power without
     * risking his true self.
     */
    public static final Habit AVATAR_MAIN_BODY_SPLIT = new Habit(
            "AVATAR_MAIN_BODY_SPLIT",
            "Avatar / Main-Body Split — Operates Through Avatars While the Main Body Stays Safe",
            "Wang Lin's risk-mitigation habit at the highest level. He maintains multiple "
                    + "avatars — the Big-Headed Elder (a disguised projection used for information "
                    + "gathering and trade), the Slaughter-clone (a combat-focused avatar drawing on "
                    + "his Slaughter Essence) — and operates through them in dangerous situations "
                    + "while his main body remains in a secured location (typically inside the "
                    + "Heaven-Defying Bead's interior). If an avatar is destroyed, he loses a "
                    + "fraction of power but survives. This habit is the late-game expression of "
                    + "EXTREME_CAUTION: never risk the true self when a proxy will do.",
            "Whenever operating in genuinely lethal territory where main-body death is possible.",
            "EXTREME_CAUTION",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 600+ (Big-Headed Elder avatar)",
                            "E24 (Thunder Celestial Tournament — avatar deployment)",
                            "E30+ (Slaughter-clone — Slaughter Essence)"),
                    4,
                    "Avatar use is canon-attested; the systematic main-body-safety discipline is "
                            + "inferred from his consistent caution, confidence 4.")
    );

    /**
     * The complete list of Wang Lin's 11 canon behavioural habits.
     * Used by the {@link WangLinPersonality} bootstrap.
     */
    public static final List<Habit> ALL_HABITS = List.of(
            DIVINE_SENSE_SCOUTING,
            ESCAPE_TALISMAN_READINESS,
            RESOURCE_HOARDING,
            PUPPET_IMMORTAL_GUARD_MAINTENANCE,
            RESTRICTION_PRACTICE,
            BEAD_TIME_DILATION_CULTIVATION,
            CULTIVATION_LEVEL_DISGUISE,
            INTENTION_TESTING,
            ENEMY_SOUL_STORAGE,
            MORTAL_LIFE_COMPREHENSION,
            AVATAR_MAIN_BODY_SPLIT
    );

    /** Look up a habit by its habitId. */
    public static Habit byId(String habitId) {
        if (habitId == null) return null;
        for (Habit h : ALL_HABITS) {
            if (h.habitId().equals(habitId)) return h;
        }
        return null;
    }
}
