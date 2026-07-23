package dev.ergenverse.simulation.settlement;

/**
 * ActorProfile — the lightweight traits that drive an actor's <b>immediate</b>
 * reasoning over a {@link WorldSituation}.
 *
 * <p>Per the user's architectural directive (the cycle after SettlementThreatIndex):
 * <blockquote>
 * Eventually it should become: World → Meaning → Reasoning → Decision → Presence.
 * Every actor reasons over the exact same world. They simply reach different
 * conclusions.
 * <br><br>
 * Wang Lin thinks: Weak wolves. No danger. Observe. Maybe useful. Protect
 * family if necessary. Don't reveal strength.
 * <br>
 * Farmer Chen thinks: Wolf. Run.
 * <br>
 * Village hunter thinks: Grab spear. Protect sheep.
 * <br>
 * Merchant thinks: Pack goods. Hide.
 * <br><br>
 * Same event. Different minds.
 * </blockquote>
 *
 * <p>An {@code ActorProfile} is the <b>"different minds"</b> — the per-actor
 * lens that turns one shared {@link WorldSituation} into many different
 * {@link Activity} decisions. It is deliberately lightweight: a handful of
 * traits, no cognition pipeline, no goal tree. The heavy cognition
 * ({@code dev.ergenverse.simulation.cognition.DecisionEngine}) handles
 * long-term planning; this profile handles the moment-to-moment "what do I do
 * about this wolf right now."
 *
 * <h2>The traits</h2>
 * <ul>
 *   <li><b>cultivationTier</b> — MORTAL, QI_CONDENSATION, FOUNDATION, etc.
 *       Determines whether the actor can even perceive a threat as "weak" and
 *       whether they have the power to confront it.</li>
 *   <li><b>courage</b> — 0.0–1.0. How readily the actor confronts danger vs.
 *       flees. Modified by the settlement's current mood (a fearful village
 *       lowers everyone's effective courage).</li>
 *   <li><b>role</b> — FARMER, HUNTER, MERCHANT, CULTIVATOR, HIDDEN_CULTIVATOR,
 *       ELDER, CHILD, LABORER. Determines what the actor is responsible for
 *       (family, livestock, goods, the sect gate) and thus what they protect.</li>
 *   <li><b>hasFamily</b> — does this actor have family at this settlement?
 *       Family-having actors prioritize gathering/protecting family.</li>
 *   <li><b>revealedStrength</b> — has the actor publicly demonstrated their
 *       true power? A hidden cultivator (Wang Lin) will NOT reveal strength
 *       against weak wolves — he observes from cover instead.</li>
 *   <li><b>combatConfidence</b> — 0.0–1.0. The actor's self-assessed ability to
 *       prevail against the current threat. A high-combatConfidence
 *       hidden cultivator assesses mortal wolves as trivially survivable and
 *       chooses to observe rather than flee.</li>
 * </ul>
 *
 * <h2>Canon vs Simulation (Article XLIV §3)</h2>
 * <p>Canon NPCs (Wang Lin, Wang Tianshui) carry profiles authored from the
 * novels. Simulation NPCs (Da Niu, Wang Wei) carry generated profiles that
 * never contradict canon. The {@link ActorProfileRegistry} documents which is
 * which.
 */
public final class ActorProfile {

    /** Cultivation tier — determines perception and combat capability. */
    public enum CultivationTier {
        MORTAL,             // no cultivation — flees from any magical threat
        QI_CONDENSATION,    // entry cultivator — can sense qi, weak combat
        FOUNDATION,         // mid cultivator — can confront spirit beasts
        CORE,               // powerful — confronts most threats
        NASCENT_SOUL,       // apex — trivializes mortal threats
        SOUL_FORMATION      // legendary — reality-bending
    }

    /** Social role — determines what the actor protects and how. */
    public enum Role {
        /** Subsistence farmer. Protects family. Flees. */
        FARMER,
        /** Has weapons and skill. Protects livestock/perimeter. Guards. */
        HUNTER,
        /** Values goods. Packs and hides assets before fleeing. */
        MERCHANT,
        /** Open cultivator. May guard or observe based on confidence. */
        CULTIVATOR,
        /**
         * Concealed cultivator (Wang Lin). Appears weak; is not. Observes
         * threats from cover; intervenes only to protect family; never reveals
         * strength against trivial threats.
         */
        HIDDEN_CULTIVATOR,
        /** Village elder. Wisdom over strength. Organizes defense or retreat. */
        ELDER,
        /** Child. Always flees to family. */
        CHILD,
        /** Laborer. Responsible for livestock/tools. Secures assets then guards. */
        LABORER,
        /** Homemaker. Gathers children, bars the door. */
        HOMEMAKER
    }

    /** The actor id this profile describes. */
    public final String actorId;

    /** Display name (for memory/chronicle entries). */
    public final String displayName;

    /** Cultivation tier. */
    public final CultivationTier cultivationTier;

    /** Base courage 0.0–1.0 (before settlement-mood modification). */
    public final float courage;

    /** Social role. */
    public final Role role;

    /** Has family at this settlement (gathers/protects them first). */
    public final boolean hasFamily;

    /** Has publicly revealed true strength (hidden cultivators: false). */
    public final boolean revealedStrength;

    /** Self-assessed combat confidence 0.0–1.0 against typical threats. */
    public final float combatConfidence;

    /** Whether this profile is canon-sourced or simulation-generated. */
    public final boolean canonSourced;

    public ActorProfile(String actorId, String displayName,
                        CultivationTier cultivationTier, float courage, Role role,
                        boolean hasFamily, boolean revealedStrength,
                        float combatConfidence, boolean canonSourced) {
        this.actorId = actorId;
        this.displayName = displayName;
        this.cultivationTier = cultivationTier;
        this.courage = clamp01(courage);
        this.role = role;
        this.hasFamily = hasFamily;
        this.revealedStrength = revealedStrength;
        this.combatConfidence = clamp01(combatConfidence);
        this.canonSourced = canonSourced;
    }

    /**
     * Effective courage after settlement-mood modification. A FEARFUL settlement
     * lowers everyone's courage; a COMPETITIVE sect raises it. This is where
     * "settlement personality influences embedded actors" (the user's directive)
     * enters the reasoning.
     */
    public float effectiveCourage(SettlementPersonality.Mood mood) {
        SettlementPersonality.Mood m = mood != null ? mood : SettlementPersonality.Mood.PEACEFUL;
        float moodMod = switch (m) {
            case PEACEFUL -> 0.0f;
            case ANXIOUS -> -0.10f;
            case FEARFUL -> -0.20f;
            case COMPETITIVE -> +0.10f;
        };
        return clamp01(courage + moodMod);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
