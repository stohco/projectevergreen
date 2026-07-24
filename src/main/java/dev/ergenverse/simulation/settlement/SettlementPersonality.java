package dev.ergenverse.simulation.settlement;

/**
 * SettlementPersonality — the collective temperament of a settlement.
 *
 * <p>Per the user's architectural directive:
 * <blockquote>
 * Settlements should have personalities. Not just NPCs.
 * <br><br>
 * Wang Family Village:
 *   Mood: Peaceful. Fear: Teng Family taxes. Identity: Poor farming village.
 *   Recent Memory: Wolf attack. Rumors: Recruiter arrived. Prosperity: Low.
 *   Security: Weak. Cultivation Level: Mortal.
 * <br>
 * Heng Yue Sect:
 *   Mood: Competitive. Identity: Cultivation sect. Politics: Factional.
 *   Danger: Medium. Prestige: High. Recruitment: Active.
 * <br><br>
 * Then NPC behavior isn't just influenced by themselves. It's influenced by the
 * place they're embedded in. That creates another layer of emergence.
 * </blockquote>
 *
 * <p>A {@code SettlementPersonality} is the <b>place-level</b> lens that shapes
 * every actor's reasoning within the settlement. A frightened village lowers
 * every embedded actor's effective courage; a competitive sect raises it. The
 * {@link ActorProfile#effectiveCourage(SettlementPersonality)} method applies
 * this modification, so the same Wang Lin would reason slightly differently
 * embedded in a peaceful village vs. a fearful one under siege.
 *
 * <p>This is NOT a new subsystem — it is a small value object on
 * {@link Settlement}, read by {@link ActorReasoningEngine}. It produces
 * observable behavior: the same wolf event produces more fleeing in a fearful
 * village, more guarding in a competitive sect.
 */
public final class SettlementPersonality {

    /** Collective mood — modifies embedded actors' effective courage. */
    public enum Mood {
        /** Calm, safe, routine. No courage modifier. */
        PEACEFUL,
        /** On edge, watchful. Slight courage penalty. */
        ANXIOUS,
        /** Under threat or in decline. Larger courage penalty. */
        FEARFUL,
        /** Vying for status/resources. Slight courage bonus. */
        COMPETITIVE
    }

    /** Collective self-image — flavor for memory/chronicle entries. */
    public enum Identity {
        POOR_FARMING_VILLAGE,
        CULTIVATION_SECT,
        TRADING_POST,
        COUNTY_SEAT,
        IMMORTAL_CAPITAL
    }

    /** Collective cultivation level — the modal tier of the population. */
    public enum SettlementCultivationLevel {
        MORTAL,             // mostly mortals (Wang Family Village)
        QI_CONDENSATION,    // entry-level sect (Heng Yue outer disciples)
        FOUNDATION,         // mid-tier sect
        CORE,               // powerful sect
        MIXED               // settlement spans multiple tiers
    }

    /** Current collective mood. */
    public Mood mood;

    /** What this place considers itself to be. */
    public Identity identity;

    /** The settlement's modal cultivation level. */
    public SettlementCultivationLevel cultivationLevel;

    /** What the settlement collectively fears (e.g. "Teng Family taxes", "wolf packs"). */
    public String primaryFear;

    /** Prosperity 0.0–1.0 (low = poor, high = wealthy). */
    public float prosperity;

    /** Security 0.0–1.0 (low = defenseless, high = well-defended). */
    public float security;

    /** The most recent significant event the settlement collectively remembers. */
    public String recentMemory;

    /** A currently circulating rumor (empty if none). */
    public String currentRumor;

    public SettlementPersonality(Mood mood, Identity identity,
                                 SettlementCultivationLevel cultivationLevel,
                                 String primaryFear, float prosperity,
                                 float security, String recentMemory,
                                 String currentRumor) {
        this.mood = mood;
        this.identity = identity;
        this.cultivationLevel = cultivationLevel;
        this.primaryFear = primaryFear;
        this.prosperity = clamp01(prosperity);
        this.security = clamp01(security);
        this.recentMemory = recentMemory;
        this.currentRumor = currentRumor;
    }

    /**
     * Record a new significant event as the settlement's recent memory.
     * Called by {@link ActorMaterializer} when a threat-response activity is
     * assigned — this is the "village remembers" half of the golden path.
     */
    public void remember(String memory) {
        this.recentMemory = memory;
    }

    /** Update the circulating rumor. */
    public void circulateRumor(String rumor) {
        this.currentRumor = rumor;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
