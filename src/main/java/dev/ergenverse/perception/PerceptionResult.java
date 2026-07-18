package dev.ergenverse.perception;

import dev.ergenverse.canon.CanonEngine;

/**
 * The result of an observer perceiving an objective thing.
 *
 * <p>This is <b>what the observer understands</b> — not what the thing
 * <b>is</b>. The thing's {@link ObjectiveNature} is unchanged by being
 * observed.
 *
 * <p>A PerceptionResult answers:
 * <ul>
 *   <li><b>perceivedName</b> — what the observer calls the thing. A
 *       mortal sees a Spirit Rabbit and calls it "a very large rabbit."
 *       A Qi Condensation cultivator calls it "Spirit Beast (rank
 *       unknown)." A Nascent Soul calls it "Spirit Rabbit, Fifth Rank."</li>
 *   <li><b>perceivedDescription</b> — what the observer thinks about
 *       the thing. Mortal: "That's the biggest rabbit I've ever seen..."
 *       Qi: "Spirit Beast. Leave. Now." Beast tamer: "Its bloodline is
 *       unusually pure." Fourth Step: "Its ancestor lineage traces back
 *       to an extinct divine hare."</li>
 *   <li><b>recognizedRank</b> — the rank the observer perceives (may be
 *       null if they can't tell). A mortal perceives no rank; a Qi
 *       cultivator perceives "spirit beast" without specific rank; a
 *       Nascent Soul perceives "Fifth Rank."</li>
 *   <li><b>canInteract</b> — can the observer interact with this thing?
 *       A mortal cannot harvest a spirit herb (its concealment formation
 *       is in spiritual space; the mortal's hands only touch the
 *       physical illusion). A Foundation cultivator can.</li>
 *   <li><b>concealed</b> — is the thing concealed from this observer?
 *       Concealment is a property of the thing (its formation, its Dao),
 *       not the observer. The observer's perception tier determines
 *       whether they penetrate the concealment.</li>
 *   <li><b>realityLevel</b> — at what canon reality level is this
 *       understanding presented? (Reality / Tradition / Rumor / Legend /
 *       Unknown — see {@link CanonEngine.RealityLevel}).</li>
 * </ul>
 */
public final class PerceptionResult {

    public final String perceivedName;
    public final String perceivedDescription;
    public final Integer recognizedRank;       // null = can't tell
    public final boolean canInteract;
    public final boolean concealed;            // the thing is concealed (and observer can't see through it)
    public final CanonEngine.RealityLevel realityLevel;
    public final PerceptionTier observerTier;

    public PerceptionResult(String perceivedName, String perceivedDescription,
                            Integer recognizedRank, boolean canInteract, boolean concealed,
                            CanonEngine.RealityLevel realityLevel, PerceptionTier observerTier) {
        this.perceivedName = perceivedName;
        this.perceivedDescription = perceivedDescription;
        this.recognizedRank = recognizedRank;
        this.canInteract = canInteract;
        this.concealed = concealed;
        this.realityLevel = realityLevel;
        this.observerTier = observerTier;
    }

    /** The observer cannot perceive this thing at all — it is concealed from them. */
    public static PerceptionResult concealed(PerceptionTier observerTier) {
        return new PerceptionResult(
            "???",
            "You sense something here, but you cannot resolve what it is. Your perception is insufficient.",
            null, false, true, CanonEngine.RealityLevel.UNKNOWN, observerTier
        );
    }

    /** The observer perceives the thing but only dimly. */
    public static PerceptionResult dim(String vagueName, String vagueDescription,
                                       PerceptionTier observerTier) {
        return new PerceptionResult(
            vagueName, vagueDescription,
            null, false, false, CanonEngine.RealityLevel.RUMOR, observerTier
        );
    }
}
