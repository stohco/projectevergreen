package dev.ergenverse.simulation.settlement;

/**
 * Motivation — a drive that shapes an actor's decisions.
 *
 * <p>Per the user's architectural directive (the cycle after the reasoning engine):
 * <blockquote>
 * Instead of ReasoningEngine → switch(profile), I'd rather see
 * Actor → evaluate(WorldSituation). Because Wang Lin is not a profile.
 * He's an individual.
 * <br><br>
 * Instead I'd score options. Wang Lin: Observe +30 curiosity +40 concealment
 * +20 cultivation opportunity -10 danger = 80. Fight +30 remove danger -80
 * reveal strength = -50. Retreat +20 safety -15 information loss = 5.
 * Highest score wins. Nobody ever wrote "Wang Lin observes wolves." Instead
 * he observes because that's what maximizes his goals.
 * </blockquote>
 *
 * <p>A {@code Motivation} is one axis along which an actor evaluates a candidate
 * activity. Each actor's {@link CultivatorMind} carries a weight per motivation
 * (how much that drive matters to them). Each candidate activity declares how
 * much it serves each motivation. The total score is the weighted sum.
 *
 * <h2>The motivations (canon-faithful, not generic)</h2>
 * <ul>
 *   <li><b>PROTECT_FAMILY</b> — keep loved ones safe. Wang Lin (parents), the
 *       patriarch, homemakers score this high.</li>
 *   <li><b>CONCEAL_STRENGTH</b> — never reveal capability unnecessarily. The
 *       defining drive of a hidden cultivator (Wang Lin at Chapter 1). Mortals
 *       have weight ~0 (nothing to conceal).</li>
 *   <li><b>CURIOSITY</b> — learn, observe, understand. Wang Lin's actual talent
 *       (RI Ch.1-5: he watches and waits). Children and scholars score it higher.</li>
 *   <li><b>SURVIVAL</b> — stay alive. Universal, but dominates mortal reasoning.</li>
 *   <li><b>DUTY</b> — fulfill one's role. The laborer's livestock responsibility,
 *       the sect disciple's gate duty, the elder's stewardship.</li>
 *   <li><b>PRESTIGE</b> — reputation, face. Sect cultivators and merchants.</li>
 *   <li><b>GREED</b> — material gain. Merchants, some sect elders.</li>
 *   <li><b>COMPASSION</b> — help others. Healers, some elders, Li Muwan (future).</li>
 *   <li><b>CULTIVATION_PROGRESS</b> — advance one's Dao. Cultivators; Wang Lin's
 *       long-term drive (observing wolves is itself a minor cultivation
 *       opportunity — watching predators teaches stillness).</li>
 * </ul>
 *
 * <p>These are not arbitrary game stats. Each maps to a theme in Er Gen's novels:
 * concealment, curiosity, duty, survival, and the pursuit of understanding are
 * the recurring drives of Renegade Immortal. Wang Lin's defining trait is that
 * CONCEAL_STRENGTH and CURIOSITY outweigh PRESTIGE and GREED — which is why he
 * observes rather than postures.
 */
public enum Motivation {
    PROTECT_FAMILY,
    CONCEAL_STRENGTH,
    CURIOSITY,
    SURVIVAL,
    DUTY,
    PRESTIGE,
    GREED,
    COMPASSION,
    CULTIVATION_PROGRESS
}
