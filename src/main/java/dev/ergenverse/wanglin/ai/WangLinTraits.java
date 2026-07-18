package dev.ergenverse.wanglin.ai;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * WangLinTraits — Wang Lin's CORE character traits, each with a {@link Provenance}.
 *
 * <p>This is the heart of the personality model: WHAT Wang Lin IS, derived
 * chapter-by-chapter from Renegade Immortal. Every trait is canon-attested —
 * Er Gen wrote the behaviour, the trait name is the simulation's
 * generalization of that behaviour. No trait is invented. Where Er Gen
 * expressed a trait only through action (not through narrator statement), the
 * attestation is INFERRED with confidence 3-4 and the ambiguity is noted.
 *
 * <h2>Trait inventory (14 traits)</h2>
 * <ol>
 *   <li><b>EXTREME_CAUTION</b> — always plans an escape route. His defining
 *       combat trait.</li>
 *   <li><b>RUTHLESSNESS_TO_ENEMIES</b> — kills without hesitation once
 *       committed, leaves no future threats.</li>
 *   <li><b>DEVOTION_TO_LI_MUWAN</b> — his entire late-game motivation.</li>
 *   <li><b>FILIAL_DEVOTION</b> — to his parents Wang Tianshui and Zhou Tingsu;
 *       their deaths are his defining trauma.</li>
 *   <li><b>PATERNAL_LOVE</b> — for Wang Ping (adopted mortal son) and
 *       Wang Yiyi (daughter).</li>
 *   <li><b>GRATITUDE_TO_MENTORS</b> — Situ Nan, Tu Si, Bai Fan, Dun Tian,
 *       Dao Master Ling Tianhou, Xuan Luo.</li>
 *   <li><b>PATIENCE</b> — willing to wait decades/centuries for revenge or
 *       opportunity. 700 years to attempt Li Muwan's resurrection.</li>
 *   <li><b>VENGEFULNESS</b> — never forgets a grudge; pursues across
 *       lifetimes (Teng Clan, All-Seer, Seven-Colored Daoist).</li>
 *   <li><b>PRAGMATISM_OVER_PRIDE</b> — flees, disguises, pretends weakness
 *       when the situation demands.</li>
 *   <li><b>LONELINESS_ISOLATION</b> — "2,000 years alone" passage (Ch. 1433);
 *       increasingly inhuman as he cultivates.</li>
 *   <li><b>MORTAL_WORLD_ATTACHMENT</b> — lives among mortals to comprehend
 *       Life-Death; never fully detaches.</li>
 *   <li><b>HEAVEN_DEFYING_WILL</b> — third Heaven-Defying Cultivator (Ch. 599);
 *       defies heaven's retribution rather than submitting.</li>
 *   <li><b>RESTRICTION_OBSESSION</b> — 7 years at Restriction Mountain;
 *       lifelong pursuit of restriction mastery.</li>
 *   <li><b>REACTIVE_NOT_INITIATING</b> — does not seek conflict; responds
 *       with overwhelming force when wronged or when his people are
 *       threatened.</li>
 * </ol>
 *
 * <h2>How the AI uses these traits</h2>
 * <p>Each trait is a weighted disposition. A goal-based planner (future,
 * Layer 2) consults the trait list when ranking candidate actions. For
 * example: when the player suggests an aggressive frontal assault, EXTREME_
 * CAUTION and PRAGMATISM_OVER_PRIDE both vote NO; if Li Muwan is at stake,
 * DEVOTION_TO_LI_MUWAN overrides them and votes YES with maximum force.
 *
 * <p><b>Per the Prime Directive:</b> trait names are simulation design;
 * trait BEHAVIOURS are canon. Where a trait is marked INFERRED, the ambiguity
 * note explains what canon attests vs what the simulation generalizes.
 */
public final class WangLinTraits {

    private WangLinTraits() {}

    /**
     * A single Wang Lin core trait.
     *
     * @param traitId      stable identifier (e.g. {@code "EXTREME_CAUTION"})
     * @param name         display name
     * @param description  one-paragraph canon-grounded description
     * @param weight       0.0-1.0 — how strongly this trait dominates Wang Lin's
     *                     behaviour relative to the others. Derived from canon
     *                     emphasis (e.g. DEVOTION_TO_LI_MUWAN is 1.0 because the
     *                     entire late novel is driven by it; REACTIVE_NOT_
     *                     INITIATING is 0.7 because he does occasionally act
     *                     proactively, e.g. hunting the Teng Clan).
     * @param provenance   source novel, chapters, attestation, confidence,
     *                     ambiguities
     */
    public record Trait(
            String traitId,
            String name,
            String description,
            double weight,
            Provenance provenance
    ) {
        public Trait {
            if (traitId == null || traitId.isBlank()) {
                throw new IllegalArgumentException("Trait requires a traitId");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Trait '" + traitId + "' requires a name");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Trait '" + traitId + "' requires a description");
            }
            if (provenance == null) {
                throw new IllegalArgumentException("Trait '" + traitId + "' requires a Provenance");
            }
            if (weight < 0.0) weight = 0.0;
            if (weight > 1.0) weight = 1.0;
        }
    }

    // ───────────────────────────────────────────────────────────────────
    //  THE 14 CANON TRAITS
    // ───────────────────────────────────────────────────────────────────

    /**
     * EXTREME_CAUTION — Wang Lin's defining combat trait. He plans an escape
     * route before engaging, scouts with divine sense, keeps a teleportation
     * restriction ready, and prefers ambush/disguise/flee over frontal
     * confrontation. Canon: his entire Zhao Country arc (after the Teng Clan
     * massacre, E13) is shaped by never being caught off-guard again; he
     * spent years in the Sea of Devils and Foreign Battleground taking zero
     * unnecessary risks.
     */
    public static final Trait EXTREME_CAUTION = new Trait(
            "EXTREME_CAUTION",
            "Extreme Caution — Always Plans an Escape Route",
            "Wang Lin never enters a situation without knowing how to leave it. "
                    + "After the Teng Clan massacre of his family (E13), caution became his "
                    + "reflexive combat stance: scout with divine sense, prepare a teleportation "
                    + "restriction, disguise his cultivation level, and prefer ambush or flight "
                    + "over honour-bound confrontation. The Sea of Devils and Foreign Battleground "
                    + "arcs are structured around him taking zero unnecessary risks.",
            0.95,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 134 (Earth Escape)", "Ch. 180 (Illusionary Circle)",
                            "Ch. 493 (Teleportation Restriction)", "E13-E15 (Teng Clan arc → Sea of Devils)"),
                    5,
                    "Cited as canonical pattern across the Zhao Country and Sea of Devils arcs; "
                            + "no single chapter names the trait, but the behaviour is consistent "
                            + "throughout. Cross-referenced CANON_RI_TIMELINE.md E13-E15.")
    );

    /**
     * RUTHLESSNESS_TO_ENEMIES — once Wang Lin decides someone is an enemy, he
     * kills them without hesitation and exterminates their lineage to prevent
     * future retaliation. Canon: the Teng Clan extermination (E14); killing
     * Sun Zhenwei at the wedding (E47); destroying multiple planets in the
     * Thunder Celestial Tournament arc (E24).
     */
    public static final Trait RUTHLESSNESS_TO_ENEMIES = new Trait(
            "RUTHLESSNESS_TO_ENEMIES",
            "Ruthlessness Toward Enemies — Kills Without Hesitation, Leaves No Future Threat",
            "Once Wang Lin commits to treating someone as an enemy, he kills them "
                    + "without hesitation and (where the threat is lineal) exterminates the "
                    + "lineage to prevent future retaliation. The Teng Clan was annihilated "
                    + "after they killed his parents (E14); Sun Zhenwei was killed at his own "
                    + "wedding (E47); Wang Lin destroyed multiple planets during the Thunder "
                    + "Celestial Tournament when the Yao Family issued a kill-order on him (E24). "
                    + "He does not grant second chances to declared enemies.",
            0.9,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 86 (Foundation Stealing)", "E14 (Teng Clan extermination)",
                            "E24 (Thunder Celestial Tournament)", "E47 (Sun Zhenwei killed at wedding)"),
                    5,
                    "Explicit and consistent across the entire novel.")
    );

    /**
     * DEVOTION_TO_LI_MUWAN — Wang Lin's entire late-game motivation. After
     * Li Muwan's death (E28), every action serves her resurrection. He kept
     * her Nascent Soul in the Heaven-Defying Bead for 700 years, attempted
     * the Qi Xi Spell at the cost of his own vitality (E88), and finally
     * resurrected and Transcended with her (E36).
     */
    public static final Trait DEVOTION_TO_LI_MUWAN = new Trait(
            "DEVOTION_TO_LI_MUWAN",
            "Devotion to Li Muwan — His Entire Late-Game Motivation",
            "Li Muwan is Wang Lin's wife and the centre of his emotional universe. "
                    + "After her body perished during Nascent Soul formation (E28), he stored her "
                    + "Nascent Soul in the Heaven-Defying Bead for 700 years, attempted the Qi Xi "
                    + "Spell (sacrificing his own vitality) to rebuild her body (E88), and finally "
                    + "resurrected and Transcended with her (E36). Every late-game decision — "
                    + "becoming the Cave World owner, killing the Seven-Colored Daoist, crossing "
                    + "the Heaven Trampling Bridges — ultimately serves being with her eternally.",
            1.0,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 1433 (the '2,000 years alone' passage)", "E28 (Li Muwan's death)",
                            "E88 (Qi Xi Spell attempt)", "E36 (Transcendence with Li Muwan)"),
                    5,
                    "Author-confirmed: Li Muwan is the official main female lead and Wang Lin's "
                            + "only wife (per wiki author Q&A; cross-referenced in wang_lin_relationships.md).")
    );

    /**
     * FILIAL_DEVOTION — Wang Lin's devotion to his parents Wang Tianshui and
     * Zhou Tingsu. Their slaughter by the Teng Clan (E13) is his defining
     * trauma; he rescued their souls from Teng Huayuan's soul flag (E14).
     */
    public static final Trait FILIAL_DEVOTION = new Trait(
            "FILIAL_DEVOTION",
            "Filial Devotion — To His Parents Wang Tianshui and Zhou Tingsu",
            "Wang Lin's parents — Wang Tianshui (father) and Zhou Tingsu (mother) — "
                    + "are his mortal-world anchor. The Teng Clan's slaughter of the Wang family "
                    + "(E13) is the defining trauma of his life; it triggered his Slaughter Dao and "
                    + "his lifelong pursuit of power. He systematically exterminated the Teng Clan "
                    + "and personally rescued his parents' souls from Teng Huayuan's soul flag (E14). "
                    + "His childhood nickname was 'Tie Zhu' (Iron Pillar), given by his parents to "
                    + "ward off bad fortune (E08).",
            0.85,
            Provenance.explicit("Renegade Immortal",
                    List.of("E08 (birth)", "E09 (childhood)", "E13 (Teng Clan massacre)",
                            "E14 (revenge, soul rescue)"),
                    5,
                    "Cross-referenced CANON_RI_TIMELINE.md E08-E14 and CANON_RI_CHARACTER_DECISIONS.md CD-02.")
    );

    /**
     * PATERNAL_LOVE — Wang Lin's love for his children. Wang Ping (mortal
     * adopted son, raised intentionally as mortal, E50) and Wang Yiyi
     * (daughter, eventually brought back to the Immortal Astral Continent).
     */
    public static final Trait PATERNAL_LOVE = new Trait(
            "PATERNAL_LOVE",
            "Paternal Love — For Wang Ping (Adopted Son) and Wang Yiyi (Daughter)",
            "Wang Lin lived a mortal lifetime raising his adopted son Wang Ping in a "
                    + "desolate village (E50, Ch. 701). Wang Ping was raised intentionally as a "
                    + "mortal — Wang Lin wanted him to experience ordinary life free from the "
                    + "cultivation world's horrors. Wang Ping's death triggered Wang Lin's Karma "
                    + "Domain evolution (CD-31). Wang Yiyi (Wang Lin and Li Muwan's daughter, "
                    + "CD-32) eventually returned to the Immortal Astral Continent with Wang Lin "
                    + "after the AWWP cross-novel arc. Wang Lin's paternal love is gentle, "
                    + "protective, and deliberately shields the child from his cosmic-scale conflicts.",
            0.8,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 701 (Wang Ping)", "E50 (mortal life with Wang Ping)",
                            "CD-31 (Wang Ping)", "CD-32 (Wang Yiyi)"),
                    5,
                    "Wang Ping's mother is Liu Mei / Mu Bingmei (complicated; see wang_lin_relationships.md). "
                            + "Wang Yiyi's mother is Li Muwan.")
    );

    /**
     * GRATITUDE_TO_MENTORS — Wang Lin never forgets those who taught him.
     * The defining quote (from wang_lin_relationships.md): "I, Wang Lin,
     * would never kneel to the heavens, only in respect for Situ!"
     */
    public static final Trait GRATITUDE_TO_MENTORS = new Trait(
            "GRATITUDE_TO_MENTORS",
            "Gratitude and Loyalty to Mentors — Situ Nan, Tu Si, Bai Fan, Dun Tian, Lu Yun, Ling Tianhou, Xuan Luo",
            "Wang Lin repays every transmission and life-saving favour across centuries. "
                    + "His defining quote about Situ Nan: 'I, Wang Lin, would never kneel to the "
                    + "heavens, only in respect for Situ!' He inherited Tu Si's Ancient God Tactic "
                    + "(E17) and treats the inheritance as a sacred trust. He carries Dun Tian's "
                    + "Soul Refining Sect inheritance (E45) and elevated it to 6th-level as promised. "
                    + "He honoured Zhou Yi's Wending Crystal by protecting the celestial corpse "
                    + "eternally (E53). His gratitude is not sentimental — it is structural; he "
                    + "treats received transmissions as life-debts.",
            0.85,
            Provenance.explicit("Renegade Immortal",
                    List.of("wang_lin_relationships.md (Situ Nan quote)",
                            "E12 (Situ Nan transmits Underworld Ascension Method)",
                            "E17 (Tu Si Ancient God inheritance)", "E45 (Dun Tian Soul Refining Sect)",
                            "E46 (Lu Yun 5th-Gen Vermilion Bird)", "E53 (Zhou Yi Wending Crystal)",
                            "E60 (Bai Fan inheritance)", "E84 (Xuan Luo on IAC)"),
                    5,
                    "Multiple mentor figures across multiple arcs; the Situ Nan quote is wiki-attested "
                            + "as canon.")
    );

    /**
     * PATIENCE — Wang Lin's defining temporal disposition. He waited 700
     * years to attempt Li Muwan's resurrection. He spent 7 years at the
     * Restriction Mountain trial. He spent decades living as a mortal to
     * comprehend Life-Death.
     */
    public static final Trait PATIENCE = new Trait(
            "PATIENCE",
            "Patience — Willing to Wait Decades or Centuries for Revenge or Opportunity",
            "Wang Lin operates on multi-decade and multi-century timelines without "
                    + "restlessness. He waited 700 years to attempt Li Muwan's resurrection via the "
                    + "Qi Xi Spell (E88). He spent 7 years mastering ancient restrictions at the "
                    + "Restriction Mountain trial (E43). He lived full mortal lifetimes to comprehend "
                    + "Life-Death (E18, E50). His decision style (per CANON_RI_CHARACTER_DECISIONS.md "
                    + "CD-02) is patient_planner + protective_loyalist.",
            0.9,
            Provenance.explicit("Renegade Immortal",
                    List.of("E43 (Restriction Mountain 7 years)", "E18 (mortal life for Soul Formation)",
                            "E50 (mortal life with Wang Ping)", "E88 (700-year wait for Qi Xi Spell)",
                            "CD-02 (decision style: patient_planner)"),
                    5,
                    "Patience is a narrator-attested disposition in CD-02.")
    );

    /**
     * VENGEFULNESS — Wang Lin never forgets a grudge. He pursued the Teng
     * Clan to extermination. He killed the All-Seer (E22) and the
     * Seven-Colored Daoist (E30). He pursued Daoist Water across multiple
     * battles until final victory (E25).
     */
    public static final Trait VENGEFULNESS = new Trait(
            "VENGEFULNESS",
            "Vengefulness — Never Forgets a Grudge, Pursues Across Lifetimes",
            "Wang Lin does not forgive wrongs done to him or his. The Teng Clan was "
                    + "exterminated to the last member after they killed his family (E14). The "
                    + "All-Seer, who plotted to possess his body, was devoured (E22). Daoist Water, "
                    + "who shattered his Crystal Sword and killed his allies, was slain (E25). The "
                    + "Seven-Colored Daoist, who created the Cave World farm that made Wang Lin "
                    + "livestock, was killed and replaced (E30). Vengeance is not impulsive — it is "
                    + "scheduled. He will wait centuries for the right moment.",
            0.85,
            Provenance.explicit("Renegade Immortal",
                    List.of("E14 (Teng Clan)", "E22 (All-Seer)", "E25 (Daoist Water)",
                            "E30 (Seven-Colored Daoist)", "CD-02 ('He does not forget grudges')"),
                    5,
                    "CD-02 explicitly states 'He does not forget grudges.'")
    );

    /**
     * PRAGMATISM_OVER_PRIDE — Wang Lin will flee, disguise his cultivation
     * level, and pretend weakness when the situation demands. He learned
     * the Disguising Technique early (Ch. 33) and used it throughout his
     * career.
     */
    public static final Trait PRAGMATISM_OVER_PRIDE = new Trait(
            "PRAGMATISM_OVER_PRIDE",
            "Pragmatism Over Pride — Flees, Disguises, Pretends Weakness When Needed",
            "Wang Lin does not let pride override survival. He learned the Disguising "
                    + "Technique (Ch. 33) to hide his cultivation level and used it throughout his "
                    + "career. He fled from Daoist Water when first battle went badly (E59), "
                    + "regrouped, and returned victorious. He pretended to be Ma Liang (a body he "
                    + "occupied) and let Li Muwan not recognize him for years. He used Foundation "
                    + "Stealing Technique (Ch. 84) rather than cultivate honorably when the path "
                    + "to Foundation Establishment was closed to him. Pragmatism is not cowardice — "
                    + "it is the long-game calculus of a man who cannot die before Li Muwan is "
                    + "resurrected.",
            0.8,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 33 (Disguising Technique)", "Ch. 84 (Foundation Stealing Technique)",
                            "E59 (first battle with Daoist Water — retreated)",
                            "wang_lin_relationships.md (Ma Liang body / Li Muwan arc)"),
                    5,
                    "Explicit and consistent across the novel.")
    );

    /**
     * LONELINESS_ISOLATION — the "2,000 years alone" passage (Ch. 1433)
     * is the canon-attested articulation of this trait. Wang Lin becomes
     * increasingly inhuman as he cultivates, his indifference a defence
     * against loss.
     */
    public static final Trait LONELINESS_ISOLATION = new Trait(
            "LONELINESS_ISOLATION",
            "Loneliness and Isolation — Increasingly Inhuman as He Cultivates",
            "Direct canon quote (Ch. 1433): 'Wang Lin had spent 2,000 years alone. He had "
                    + "grown accustomed to being lonely. His indifferent expression wasn't something "
                    + "he made deliberately; it was just after spending 2,000 years alone, he didn't "
                    + "know any other expression. He only knew indifference… He rarely smiled… The "
                    + "only thing that remained was the sorrow that couldn't be erased hidden deep "
                    + "under his indifferent eyes. It was sorrow that had accompanied him all his "
                    + "life.' His isolation is not chosen — it is the inevitable consequence of "
                    + "outliving everyone he cares about except Li Muwan.",
            0.9,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 1433 (the '2,000 years alone' passage)"),
                    5,
                    "Direct canon narrator statement, the strongest single attestation of any Wang Lin trait.")
    );

    /**
     * MORTAL_WORLD_ATTACHMENT — despite his cosmic power, Wang Lin keeps
     * returning to the mortal world. He lives among mortals to comprehend
     * Life-Death (E18, E50). His parents were mortal. Wang Ping was raised
     * mortal. He does not fully detach into inhuman detachment.
     */
    public static final Trait MORTAL_WORLD_ATTACHMENT = new Trait(
            "MORTAL_WORLD_ATTACHMENT",
            "Attachment to the Mortal World Despite His Power",
            "Wang Lin returns to mortal life at key cultivation junctures — not as "
                    + "vacation but as Dao comprehension. He lived among mortals to comprehend "
                    + "Life-Death and break through to Soul Formation (E18). He lived another full "
                    + "mortal lifetime raising Wang Ping (E50). His parents were mortal; he carries "
                    + "their mortal-scale love as part of his Dao. This trait MODIFIES "
                    + "LONELINESS_ISOLATION — he is isolated from cultivator peers but maintains "
                    + "mortal-scale tenderness with Li Muwan, Wang Ping, and Wang Yiyi.",
            0.7,
            Provenance.explicit("Renegade Immortal",
                    List.of("E18 (mortal life for Soul Formation)", "E50 (mortal life with Wang Ping)",
                            "E08 (mortal parents)", "Ch. 1433 (mortal-scale tenderness passages)"),
                    5,
                    "Cross-referenced CANON_RI_TIMELINE.md E18 and E50.")
    );

    /**
     * HEAVEN_DEFYING_WILL — Wang Lin is the third Heaven-Defying Cultivator
     * in the Cave World's history (E51, Ch. 599). Heaven-Defying means his
     * cultivation goes against the will of heaven — and survives the
     * retribution. He embraced his inner demons on the 3rd Heaven Trampling
     * Bridge rather than sealing them (E34).
     */
    public static final Trait HEAVEN_DEFYING_WILL = new Trait(
            "HEAVEN_DEFYING_WILL",
            "Heaven-Defying Will — Defies Heaven's Retribution Rather Than Submitting",
            "Wang Lin is the third Heaven-Defying Cultivator in the Cave World's history "
                    + "(E51, Ch. 599). Heaven-Defying means his Dao goes against heaven's will — "
                    + "and he survives the retribution that follows. He defied the reincarnation "
                    + "cycle to put Li Muwan's soul into a baby (wang_lin_relationships.md). On the "
                    + "3rd Heaven Trampling Bridge, where the trial is to close off inner demons, "
                    + "he EMBRACED them instead, crossing via Heaven-Defying Will (E34). His 9th "
                    + "accompanying thunder, the 'Defying Thunder' (Ch. 1368), is formed via "
                    + "defying-will and has never appeared since the beginning of time.",
            0.85,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 599 (third Heaven-Defying Cultivator)", "E51",
                            "E34 (3rd Heaven Trampling Bridge — embraced inner demons)",
                            "Ch. 1368 (Defying Thunder)"),
                    5,
                    "'Heaven-Defying' is the title and disposition Er Gen explicitly gave Wang Lin.")
    );

    /**
     * RESTRICTION_OBSESSION — Wang Lin's lifelong pursuit of restriction
     * mastery. 7 years at Restriction Mountain (E43). The Four Great
     * Restrictions (E55, E55b, E55c, E55d). The Restriction Essence (E80).
     */
    public static final Trait RESTRICTION_OBSESSION = new Trait(
            "RESTRICTION_OBSESSION",
            "Restriction Obsession — Lifelong Pursuit of Restriction Mastery",
            "Wang Lin's signature Dao specialization. He spent 7 years at the Restriction "
                    + "Mountain trial in the Land of the Ancient God, becoming only the 4th person "
                    + "in history to complete it (E43). His hair turned white during those years. "
                    + "He collected all Four Great Restrictions — Annihilation (E55, Ch. 754), Time "
                    + "(Ch. 1223), Life-Death (Ch. 1229), Ancient Soul (Ch. 1697). He developed "
                    + "the Illusionary Circle technique (Ch. 180) — wave-based restriction analysis "
                    + "without needing to see the restriction. He ultimately comprehended the "
                    + "Restriction Essence itself (E80, Ch. 1715). Restriction is the through-line "
                    + "of his entire career.",
            0.85,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 179-180 (Restriction Mountain trial, Illusionary Circle)",
                            "E43", "Ch. 754 (Annihilation Restriction)",
                            "Ch. 1223 (Time Restriction)", "Ch. 1229 (Life-Death Restriction)",
                            "Ch. 1697 (Ancient Soul Restriction)", "Ch. 1715 (Restriction Essence)",
                            "E80"),
                    5,
                    "Restriction is Wang Lin's most-attested specialization — every major arc "
                            + "includes restriction content.")
    );

    /**
     * REACTIVE_NOT_INITIATING — Wang Lin does not seek conflict. He responds
     * with overwhelming force when wronged or when his people are threatened.
     * Per CANON_RI_CHARACTER_DECISIONS.md CD-02: "Trigger conditions (what
     * causes him to act) — 1. Li Muwan is threatened → immediate, maximum-
     * force response. 2. His allies or disciples are threatened → strong
     * response. 3. The Cave World's stability is threatened → response
     * proportional to threat. 4. A cultivator seeks his mentorship → he may
     * accept."
     */
    public static final Trait REACTIVE_NOT_INITIATING = new Trait(
            "REACTIVE_NOT_INITIATING",
            "Reactive, Not Initiating — Does Not Seek Conflict; Responds with Overwhelming Force When Wronged",
            "Wang Lin does not initiate conflict. He pursues his own cultivation, his own "
                    + "restriction studies, his own network — and reacts when wronged or when his "
                    + "people are threatened. Per CD-02, his trigger conditions are: (1) Li Muwan "
                    + "threatened → immediate maximum-force response; (2) allies or disciples "
                    + "threatened → strong response; (3) Cave World stability threatened → "
                    + "proportional response; (4) a cultivator seeks his mentorship → he may "
                    + "accept. He did NOT proactively hunt the Seven-Colored Daoist until the "
                    + "Daoist's farm nature made conflict unavoidable. He did NOT proactively "
                    + "hunt the All-Seer until the possession plot was revealed.",
            0.7,
            Provenance.explicit("Renegade Immortal",
                    List.of("CD-02 (trigger conditions)", "E22 (killed All-Seer after plot revealed)",
                            "E30 (killed Seven-Colored Daoist after farm revealed)"),
                    5,
                    "CD-02 explicitly enumerates Wang Lin's trigger conditions.")
    );

    /**
     * The complete list of Wang Lin's 14 canon core traits.
     * Used by the {@link WangLinPersonality} bootstrap.
     */
    public static final List<Trait> ALL_TRAITS = List.of(
            EXTREME_CAUTION,
            RUTHLESSNESS_TO_ENEMIES,
            DEVOTION_TO_LI_MUWAN,
            FILIAL_DEVOTION,
            PATERNAL_LOVE,
            GRATITUDE_TO_MENTORS,
            PATIENCE,
            VENGEFULNESS,
            PRAGMATISM_OVER_PRIDE,
            LONELINESS_ISOLATION,
            MORTAL_WORLD_ATTACHMENT,
            HEAVEN_DEFYING_WILL,
            RESTRICTION_OBSESSION,
            REACTIVE_NOT_INITIATING
    );

    /** Look up a trait by its traitId. */
    public static Trait byId(String traitId) {
        if (traitId == null) return null;
        for (Trait t : ALL_TRAITS) {
            if (t.traitId().equals(traitId)) return t;
        }
        return null;
    }
}
