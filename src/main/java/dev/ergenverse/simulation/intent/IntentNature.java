package dev.ergenverse.simulation.intent;

/**
 * IntentNature — the 17 immediate-intent types an NPC can hold.
 *
 * <p>Per the ChatGPT architectural review and Constitution Art X (Intelligence
 * Is Reasoning): "NPCs output INTENTS not actions. Identity (rarely changes)
 * → Goals (hours) → Immediate Intent (seconds) → Decision → Planner → Tasks
 * → Minecraft Goals → World Changes."
 *
 * <p>An Intent is the STRATEGIC FRAMING of the current action — not the
 * action itself, but the WHY behind it. It's what makes "walk away" mean
 * "pretend weak and observe" (AVOID_REVEALING_STRENGTH) vs "flee in terror"
 * (RETREAT_TACTICALLY) vs "go get herbs" (SEEK_OPPORTUNITY).
 *
 * <h2>Canon alignment</h2>
 * <p>Wang Lin is the canonical example. His Identity=Survivor/cautious,
 * his Goal=acquire restriction insight, but his IMMEDIATE INTENT in most
 * encounters is AVOID_REVEALING_STRENGTH — he pretends to be weak, observes
 * from a distance, and only reveals his true power when cornered. This
 * intent layer is what makes him feel like Wang Lin and not a generic NPC.
 *
 * <p><b>Provenance: INFERRED.</b> The 17 types are distilled from Wang Lin's
 * behavioral patterns across Renegade Immortal.
 */
public enum IntentNature {

    /** Pretend to be weaker than one is. Wang Lin's signature. Canon: RI Ch.1+. */
    AVOID_REVEALING_STRENGTH("Avoid revealing strength", "RI Ch.1+"),
    /** Observe from cover, scout, eavesdrop before acting. Canon: RI Ch.3+. */
    GATHER_INTEL("Gather intelligence", "RI Ch.3+"),
    /** Guard a person, item, or location. Canon: RI Ch.50+. */
    PROTECT_ASSET("Protect asset", "RI Ch.50+"),
    /** Assert dominance, display power to deter challenges. Canon: RI Ch.200+. */
    ESTABLISH_DOMINANCE("Establish dominance", "RI Ch.200+"),
    /** Search for a cultivation opportunity. Canon: RI Ch.10+. */
    SEEK_OPPORTUNITY("Seek opportunity", "RI Ch.10+"),
    /** Maintain a false identity or cover story. Canon: RI Ch.100+. */
    MAINTAIN_COVER("Maintain cover identity", "RI Ch.100+"),
    /** Test someone's judgment before trusting/teaching them. Canon: RI Ch.50+. */
    TEST_JUDGMENT("Test judgment", "RI Ch.50+"),
    /** Cultivate in secret while appearing mundane. Canon: RI Ch.5+. */
    CULTIVATE_SECRETLY("Cultivate secretly", "RI Ch.5+"),
    /** Set up a surprise attack or trap. Canon: RI Ch.30+. */
    AMBUSH("Ambush", "RI Ch.30+"),
    /** Negotiate a deal or alliance. Canon: RI Ch.80+. */
    NEGOTIATE("Negotiate", "RI Ch.80+"),
    /** Feed false information, create diversions. Canon: RI Ch.40+. */
    DECEIVE("Deceive", "RI Ch.40+"),
    /** Withdraw tactically (not fleeing in panic). Canon: RI Ch.20+. */
    RETREAT_TACTICALLY("Retreat tactically", "RI Ch.20+"),
    /** Advance when the path is open, disengage if conditions change. Canon: RI Ch.15+. */
    ADVANCE_OPPORTUNISTICALLY("Advance opportunistically", "RI Ch.15+"),
    /** Observe from a safe distance without engaging. Canon: RI Ch.7+. */
    OBSERVE_FROM_DISTANCE("Observe from distance", "RI Ch.7+"),
    /** Provoke a target into acting rashly. Canon: RI Ch.60+. */
    PROVOKE("Provoke", "RI Ch.60+"),
    /** Hold a defensive position, fortify and repel. Canon: RI Ch.90+. */
    DEFEND_POSITION("Defend position", "RI Ch.90+"),
    /** Explore an unknown area slowly and carefully. Canon: RI Ch.10+. */
    EXPLORE_CAUTIOUSLY("Explore cautiously", "RI Ch.10+");

    public final String label;
    public final String canonSource;

    IntentNature(String label, String canonSource) {
        this.label = label;
        this.canonSource = canonSource;
    }
}
