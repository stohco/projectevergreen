package dev.ergenverse.simulation.artifact;

import java.util.List;

/**
 * The output of {@link ArtifactUsageEngine#calculateUsage} — the full
 * result of a wielder attempting to use a treasure.
 *
 * <p>This is NEVER a boolean pass/fail. It is always a spectrum:
 * how much physical damage, how much spiritual damage, which abilities
 * activated, and what backlash risk the wielder faces.
 *
 * <p>Canon basis: when Wang Lin uses a treasure above his realm, the
 * novel describes the RESULT — partial effect, strain, the treasure
 * humming but not fully responding — not a simple "access denied."
 */
public record ArtifactOutput(
        /** The highest usage layer the wielder can currently express. */
        ArtifactUsageLayer expressedLayer,

        /** Physical damage this swing/action deals. Vanilla-scale. */
        double physicalDamage,

        /** Spiritual damage — damages the target's qi/soul, not HP. */
        double spiritualDamage,

        /** Which specific activated abilities actually fired. Empty at PHYSICAL. */
        List<String> activatedAbilities,

        /** Backlash risk 0..1. 0 = completely safe. 1 = almost certain severe injury. */
        double backlashRisk,

        /** The type of backlash that would occur at this risk level. */
        BacklashType backlashType,

        /**
         * A human-readable, canon-flavored description of what happens.
         * Example: "The God-Slaying Sword hums faintly in your hand.
         * It knows you cannot yet wield it."
         */
        String narrativeNote
) {
    /** A safe output — physical use only, no backlash, no abilities. */
    public static ArtifactOutput physicalOnly(double damage, String note) {
        return new ArtifactOutput(ArtifactUsageLayer.PHYSICAL, damage, 0.0,
                List.of(), 0.0, BacklashType.NONE, note);
    }

    /** An output indicating the item is sealed — no interaction possible. */
    public static ArtifactOutput sealed(String note) {
        return new ArtifactOutput(ArtifactUsageLayer.SEALED, 0.0, 0.0,
                List.of(), 0.0, BacklashType.NONE, note);
    }

    public boolean hasBacklash() {
        return backlashRisk > 0.05 && backlashType != BacklashType.NONE;
    }

    public boolean hasAbilities() {
        return activatedAbilities != null && !activatedAbilities.isEmpty();
    }
}