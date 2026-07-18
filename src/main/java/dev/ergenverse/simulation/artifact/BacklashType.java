package dev.ergenverse.simulation.artifact;

/**
 * Types of backlash a cultivator may suffer when forcing a treasure
 * beyond their capability.
 *
 * <p><b>Design principle:</b> backlash is contextual, not automatic.
 * A low-realm cultivator holding a high-realm treasure is fine. Backlash
 * occurs only when the wielder forces beyond what they can sustain.
 *
 * <p>Each type has a {@code severityFloor} — the minimum backlash risk
 * (0..1) at which this type can appear. MERIDIAN_STRAIN appears at low
 * risk; POSSESSION appears only at high risk. This prevents trivial
 * actions from causing catastrophic injury.
 *
 * <h2>Canon basis</h2>
 * <ul>
 *   <li><b>MERIDIAN_STRAIN</b> — common in RI when cultivators force
 *       breakthroughs or overdraw qi. Treatable with pills and rest.</li>
 *   <li><b>SOUL_INJURY</b> — Wang Lin's origin soul is nearly destroyed
 *       multiple times. Recovery requires soul-recovery treasures or
 *       extended cultivation. Weakened divine sense.</li>
 *   <li><b>QI_DEPLETION</b> — running out of qi mid-fight. Immediate
 *       combat incapacity. Recovers with meditation or pills.</li>
 *   <li><b>CULTIVATION_REGRESSION</b> — the most feared outcome of
 *       forced overdraw. Losing hard-won cultivation progress.</li>
 *   <li><b>SPIRIT_REJECTION</b> — a treasure spirit refuses to obey.
 *       The ability simply fails; no damage, but the spirit's resentment
 *       increases, making future attempts harder.</li>
 *   <li><b>SPIRIT_RETALIATION</b> — the spirit actively attacks the
 *       wielder. Xu Liguo occasionally threatens this; Situ Nan could
 *       have done it. Causes soul injury or possession.</li>
 *   <li><b>POSSESSION</b> — the spirit seizes control. The wielder
 *       loses agency temporarily. The item acts on its own.</li>
 *   <li><b>KARMIC_DEBT</b> — forcing a sealed restriction incurs karma
 *       that attracts tribulation lightning later. Invisible immediate
 *       effect, but catastrophic long-term consequence.</li>
 *   <li><b>PHYSICAL_EXHAUSTION</b> — swinging an impossibly heavy
 *       treasure causes fatigue. No permanent damage, just slow
 *       recovery. The mildest form of backlash.</li>
 * </ul>
 */
public enum BacklashType {

    /** No backlash. */
    NONE(0.0, "No backlash.", false),

    /** Physical fatigue from wielding something too heavy. Mildest. */
    PHYSICAL_EXHAUSTION(0.05, "You are exhausted from the weight.", false),

    /** Meridian damage — reduces max qi, slows recovery. Treatable with pills/time. */
    MERIDIAN_STRAIN(0.15, "Your meridians strain under the pressure.", true),

    /** Qi completely depleted. Immediate combat incapacity. */
    QI_DEPLETION(0.25, "Your qi is completely drained.", true),

    /** Spirit refuses. Ability fails; resentment increases. No direct damage. */
    SPIRIT_REJECTION(0.30, "The spirit rejects your command.", false),

    /** Spirit actively attacks the wielder. Causes soul injury or possession. */
    SPIRIT_RETALIATION(0.50, "The spirit retaliates against you!", true),

    /** Soul injury — reduces divine sense range, weakens spiritual activation. */
    SOUL_INJURY(0.45, "Your origin soul screams in pain.", true),

    /** Cultivation regression — losing hard-won progress. The most feared. */
    CULTIVATION_REGRESSION(0.70, "Your cultivation base shudders and regresses!", true),

    /** Spirit seizes control. Wielder loses agency. Item acts on its own. */
    POSSESSION(0.65, "The spirit overwhelms your will!", true),

    /** Karmic debt — attracts future tribulation. No immediate damage. */
    KARMIC_DEBT(0.55, "You feel the weight of karma settling upon you.", false),

    /** Heavenly karma — heaven itself takes notice. More severe than normal karmic debt. */
    HEAVENLY_KARMA(0.60, "The heavens mark your sin. Tribulation will find you.", true),

    /** Divine retaliation — the treasure's power turns back on the wielder. */
    DIVINE_RETALIATION(0.75, "The treasure's divine power reverses and strikes you!", true),

    /** Devil corruption — devil essence taints the user's soul. */
    DEVIL_CORRUPTION(0.50, "Devil essence seeps into your soul, whispering dark thoughts.", true),

    /** Heart demon — inner darkness is fed by the action. */
    HEART_DEMON(0.65, "Your inner demons grow stronger from this act.", true);

    /** The minimum backlash risk (0..1) at which this type can appear. */
    public final double severityFloor;

    /** A narrative description shown to the player. */
    public final String narrativeNote;

    /** Whether this backlash type causes actual damage (vs. just failure/debt). */
    public final boolean causesDamage;

    BacklashType(double severityFloor, String narrativeNote, boolean causesDamage) {
        this.severityFloor = severityFloor;
        this.narrativeNote = narrativeNote;
        this.causesDamage = causesDamage;
    }

    /**
     * Given a computed backlash risk, determine which type applies.
     * Returns NONE if risk is below all floors.
     */
    public static BacklashType fromRisk(double risk) {
        if (risk <= 0.0) return NONE;
        BacklashType result = NONE;
        for (BacklashType bt : values()) {
            if (risk >= bt.severityFloor && bt.severityFloor > result.severityFloor) {
                result = bt;
            }
        }
        return result;
    }
}